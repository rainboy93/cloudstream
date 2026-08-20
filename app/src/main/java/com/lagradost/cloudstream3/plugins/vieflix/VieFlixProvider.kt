package com.lagradost.cloudstream3.plugins.vieflix

import androidx.annotation.Keep

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addPoster
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mapper
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.Jsoup
import java.net.URLEncoder

class VieFlixProvider(val plugin: VieFlixPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "VieFlix"
    override var mainUrl = "https://vieflix.top"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    // vieflix.com is a landing page whose constan.js holds the current streaming
    // domain (TARGET_DOMAIN). Resolve it once so the provider follows domain changes.
    private val domainConfigUrl = "https://www.vieflix.com/constan.js"

    @Volatile
    private var domainResolved = false

    // Every section is served by /duyet-tim so it can be sorted newest-to-oldest
    // (sortField=updatedAt&sortType=desc) consistently.
    private val sort = "sortField=updatedAt&sortType=desc"

    override val mainPage = mainPageOf(
        Pair("duyet-tim?$sort", "Phim Mới Cập Nhật"),
        Pair("duyet-tim?isChieuRap=true&$sort", "Phim Chiếu Rạp Mới Nhất"),
        Pair("duyet-tim?country=han-quoc&$sort", "Phim Hàn Quốc"),
        Pair("duyet-tim?country=trung-quoc&$sort", "Phim Trung Quốc"),
        Pair("duyet-tim?country=au-my&$sort", "Phim Âu - Mỹ"),
        Pair("duyet-tim?country=nhat-ban&$sort", "Phim Nhật Bản"),
        Pair("duyet-tim?category=hoat-hinh&$sort", "Hoạt Hình"),
        Pair("duyet-tim?category=hanh-dong&$sort", "Hành Động"),
        Pair("duyet-tim?category=tinh-cam&$sort", "Tình Cảm"),
        Pair("duyet-tim?category=co-trang&$sort", "Cổ Trang"),
        Pair("duyet-tim?category=kinh-di&$sort", "Kinh Dị"),
        Pair("duyet-tim?category=hai-huoc&$sort", "Hài Hước"),
        Pair("duyet-tim?category=tv-shows&$sort", "TV Shows"),
    )

    private suspend fun resolveDomain() {
        if (domainResolved) return
        try {
            val js = app.get(domainConfigUrl).text
            val target = Regex("""TARGET_DOMAIN:\s*"([^"]+)"""").find(js)?.groupValues?.get(1)
            if (!target.isNullOrBlank()) {
                mainUrl = target.trimEnd('/')
                // Only cache on success so a failed fetch retries on the next request instead
                // of getting stuck on the hardcoded fallback for the rest of the session.
                domainResolved = true
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        resolveDomain()
        val separator = if (request.data.contains("?")) "&" else "?"
        val url = "$mainUrl/${request.data}${separator}page=$page"
        val list = getMoviesList(url)
        return newHomePageResponse(
            list = HomePageList(name = request.name, list = list, isHorizontalImages = false),
            hasNext = list.isNotEmpty()
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        resolveDomain()
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        return getMoviesList("$mainUrl/az-list?keyword=$q")
    }

    private suspend fun getMoviesList(url: String): List<SearchResponse> {
        return try {
            val document = Jsoup.parse(app.get(url).text)
            val seen = HashSet<String>()
            document.select("a[href^=/phim/]").mapNotNull { anchor ->
                val href = anchor.attr("href")
                val img = anchor.selectFirst("img") ?: return@mapNotNull null
                if (!seen.add(href)) return@mapNotNull null
                val title = img.attr("alt").ifBlank { return@mapNotNull null }
                val poster = img.attr("src").ifBlank { img.attr("data-src") }
                newMovieSearchResponse(title, "$mainUrl$href", TvType.Movie) {
                    this.posterUrl = poster
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        resolveDomain()
        val html = app.get(url).text
        val document = Jsoup.parse(html)

        val rawTitle = document.selectFirst("meta[property=og:title]")?.attr("content").orEmpty()
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
        val plot = document.selectFirst("meta[property=og:description]")?.attr("content")

        val flight = extractFlight(html)
        val title =
            flightString(flight, """"name":"((?:[^"\\]|\\.)*)","slug":"[^"]*","originName"""")
                ?: cleanTitle(rawTitle)
        val year = Regex(""""year":(\d{4})""").find(flight)?.groupValues?.get(1)?.toIntOrNull()
        val trailer = flightString(flight, """"trailerUrl":"((?:[^"\\]|\\.)*)"""")
        val tags = extractCategoryNames(flight)

        val sources = parseSources(flight)

        // Flatten every server/language episode, keyed by episode number.
        val grouped = LinkedHashMap<Int, MutableList<VieFlixStream>>()
        for (source in sources) {
            for (language in source.languages) {
                for (episode in language.episodes) {
                    val number = episode.episodeNumber ?: episodeNumberOf(episode.name) ?: 1
                    val label = listOfNotNull(source.serverName, language.name)
                        .joinToString(" - ").ifBlank { name }
                    val m3u8 = episode.linkM3u8?.trim().orEmpty()
                    val embed = episode.linkEmbed?.trim().orEmpty()
                    if (m3u8.isEmpty() && embed.isEmpty()) continue
                    grouped.getOrPut(number) { mutableListOf() }
                        .add(VieFlixStream(label = label, m3u8 = m3u8, embed = embed))
                }
            }
        }

        if (grouped.size <= 1) {
            val data = grouped.values.firstOrNull()?.toJson() ?: "[]"
            return newMovieLoadResponse(title, url, TvType.Movie, data) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
                this.tags = tags
                addPoster(poster)
                if (!trailer.isNullOrBlank()) addTrailer(trailer)
            }
        }

        val episodes = grouped.entries
            .sortedBy { it.key }
            .map { entry ->
                newEpisode(entry.value.toJson()) {
                    this.name = "Tập ${entry.key}"
                    this.episode = entry.key
                    this.posterUrl = poster
                }
            }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year = year
            this.plot = plot
            this.tags = tags
            addPoster(poster)
            if (!trailer.isNullOrBlank()) addTrailer(trailer)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val streams = tryParseJson<List<VieFlixStream>>(data) ?: return false
        var found = false

        streams.forEach { stream ->
            if (stream.m3u8.isNotEmpty()) {
                found = true
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = "$name - ${stream.label}",
                        url = stream.m3u8,
                    ) {
                        referer = "$mainUrl/"
                        quality = Qualities.Unknown.value
                        type = ExtractorLinkType.M3U8
                    }
                )
            } else if (stream.embed.isNotEmpty()) {
                if (loadExtractor(stream.embed, "$mainUrl/", subtitleCallback, callback)) {
                    found = true
                }
            }
        }

        return found
    }

    /** Concatenates and un-escapes the Next.js RSC flight payloads embedded in the page. */
    private fun extractFlight(html: String): String {
        val regex = Regex("""self\.__next_f\.push\(\[\d+,"((?:[^"\\]|\\.)*)"]\)""")
        val builder = StringBuilder()
        for (match in regex.findAll(html)) {
            val payload = match.groupValues[1]
            val unescaped = try {
                mapper.readValue("\"$payload\"", String::class.java)
            } catch (_: Exception) {
                ""
            }
            builder.append(unescaped)
        }
        return builder.toString()
    }

    private fun flightString(flight: String, pattern: String): String? {
        val raw = Regex(pattern).find(flight)?.groupValues?.get(1) ?: return null
        return try {
            mapper.readValue("\"$raw\"", String::class.java)
        } catch (_: Exception) {
            raw
        }
    }

    private fun extractCategoryNames(flight: String): List<String> {
        val array = extractArray(flight, "categories") ?: return emptyList()
        return Regex(""""name":"((?:[^"\\]|\\.)*)"""").findAll(array)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun parseSources(flight: String): List<VieFlixSource> {
        val array = extractArray(flight, "sources") ?: return emptyList()
        return tryParseJson<List<VieFlixSource>>(array).orEmpty()
    }

    /** Extracts the `"<key>":[ ... ]` array from clean (un-escaped) flight JSON. */
    private fun extractArray(flight: String, key: String): String? {
        val marker = "\"$key\":["
        val markerIndex = flight.indexOf(marker)
        if (markerIndex < 0) return null

        val start = markerIndex + marker.length - 1 // index of '['
        var depth = 0
        var inString = false
        var i = start
        while (i < flight.length) {
            val c = flight[i]
            if (inString) {
                when (c) {
                    '\\' -> {
                        i += 2
                        continue
                    }

                    '"' -> inString = false
                }
            } else {
                when (c) {
                    '[' -> depth++
                    ']' -> {
                        depth--
                        if (depth == 0) return flight.substring(start, i + 1)
                    }

                    '"' -> inString = true
                }
            }
            i++
        }
        return null
    }

    private fun cleanTitle(raw: String): String {
        return raw
            .replace(Regex("""\s*-\s*VieFlix\s*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Phim\s+""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    /** Extracts the episode's numeric index from values like "1", "Tập 10". */
    private fun episodeNumberOf(text: String?): Int? =
        text?.let { Regex("\\d+").find(it)?.value?.toIntOrNull() }

    @Keep
    data class VieFlixSource(
        @JsonProperty("serverName") val serverName: String? = null,
        @JsonProperty("serverKey") val serverKey: String? = null,
        @JsonProperty("languages") val languages: List<VieFlixLanguage> = emptyList(),
    )

    @Keep
    data class VieFlixLanguage(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("episodes") val episodes: List<VieFlixEpisode> = emptyList(),
    )

    @Keep
    data class VieFlixEpisode(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("episodeNumber") val episodeNumber: Int? = null,
        @JsonProperty("linkEmbed") val linkEmbed: String? = null,
        @JsonProperty("linkM3u8") val linkM3u8: String? = null,
        @JsonProperty("linkDirect") val linkDirect: String? = null,
    )

    @Keep
    data class VieFlixStream(
        @JsonProperty("label") val label: String = "",
        @JsonProperty("m3u8") val m3u8: String = "",
        @JsonProperty("embed") val embed: String = "",
    )
}
