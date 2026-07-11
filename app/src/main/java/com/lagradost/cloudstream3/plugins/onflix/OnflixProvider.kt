package com.lagradost.cloudstream3.plugins.onflix

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

class OnflixProvider(val plugin: OnflixPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "Onflix"
    override var mainUrl = "https://onflix.lat"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    private val apiUrl = "https://k8s.onflixcdn.com/api"

    override val mainPage = mainPageOf(
        Pair("${apiUrl}/movies", "Mới Cập Nhật/vertical"),
        Pair("${apiUrl}/movies?type=phim-le", "Phim Lẻ/horizontal"),
        Pair("${apiUrl}/movies?type=phim-bo", "Phim Bộ/vertical"),
        Pair("${apiUrl}/movies?country=han-quoc", "Phim Hàn Quốc/vertical"),
        Pair("${apiUrl}/movies?country=trung-quoc", "Phim Trung Quốc/vertical"),
        Pair("${apiUrl}/movies?country=au-my", "Phim Âu - Mỹ/vertical"),
        Pair("${apiUrl}/movies?category=hoat-hinh", "Hoạt Hình/vertical"),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val name = request.name.split("/")[0]
        val horizontal = request.name.split("/").getOrNull(1) == "horizontal"

        val url = if (request.data.contains("?")) {
            "${request.data}&page=$page"
        } else {
            "${request.data}?page=$page"
        }

        val response = tryParseJson<OnflixListResponse>(app.get(url).text)
        val list = response?.data.orEmpty().mapNotNull { it.toSearchResponse(horizontal) }

        return newHomePageResponse(
            list = HomePageList(name = name, list = list, isHorizontalImages = horizontal),
            hasNext = list.isNotEmpty()
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val response = tryParseJson<OnflixSearchResponse>(app.get("${apiUrl}/search?q=$q").text)
        return response?.movies.orEmpty().mapNotNull { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse {
        val html = app.get(url).text
        val document = Jsoup.parse(html)

        val rawTitle = document.selectFirst("meta[property=og:title]")?.attr("content")
            ?: document.selectFirst("title")?.text().orEmpty()
        val year = Regex("\\((\\d{4})\\)").find(rawTitle)?.groupValues?.get(1)?.toIntOrNull()
        val title = cleanTitle(rawTitle)
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
        val plot = document.selectFirst("meta[property=og:description]")?.attr("content")
            ?.let { Jsoup.parse(it).text() }
        val trailer = Regex("\"trailer_url\":\"(https?://[^\"]+)\"").find(extractFlight(html))
            ?.groupValues?.get(1)

        val streams = parseStreams(html)

        // Group each playable server by its episode (episodes share a slug).
        val grouped = LinkedHashMap<String, MutableList<OnflixStream>>()
        for (stream in streams) {
            val key = stream.slug ?: stream.name ?: continue
            grouped.getOrPut(key) { mutableListOf() }.add(stream)
        }

        if (grouped.size <= 1) {
            val data = grouped.values.firstOrNull()?.toJson() ?: "[]"
            return newMovieLoadResponse(title, url, TvType.Movie, data) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
                addPoster(poster)
                if (trailer != null) addTrailer(trailer)
            }
        }

        val episodes = grouped.entries
            .sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }
            .mapIndexed { index, entry ->
                newEpisode(entry.value.toJson()) {
                    this.name = entry.value.firstOrNull()?.name ?: "Tập ${index + 1}"
                    this.episode = entry.key.toIntOrNull() ?: (index + 1)
                    this.posterUrl = poster
                }
            }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year = year
            this.plot = plot
            addPoster(poster)
            if (trailer != null) addTrailer(trailer)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val streams = tryParseJson<List<OnflixStream>>(data) ?: return false
        var found = false

        streams.forEach { stream ->
            val label = stream.serverName ?: name
            val m3u8 = stream.linkM3u8?.trim().orEmpty()
            val embed = stream.linkEmbed?.trim().orEmpty()

            if (m3u8.isNotEmpty()) {
                found = true
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = "$name - $label",
                        url = m3u8,
                    ) {
                        referer = "$mainUrl/"
                        quality = Qualities.Unknown.value
                        type = ExtractorLinkType.M3U8
                    }
                )
            } else if (embed.isNotEmpty()) {
                if (loadExtractor(embed, "$mainUrl/", subtitleCallback, callback)) {
                    found = true
                }
            }
        }

        return found
    }

    private fun cleanTitle(raw: String): String {
        return raw
            .replace(Regex("\\s*-\\s*(Xem ngay trên\\s*)?ONFLIX\\s*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\(\\d{4}\\)\\s*$"), "")
            .trim()
    }

    /** Concatenates and un-escapes the Next.js RSC flight payloads embedded in the page. */
    private fun extractFlight(html: String): String {
        val regex = Regex("""self\.__next_f\.push\(\[1,"((?:[^"\\]|\\.)*)"]\)""")
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

    private fun parseStreams(html: String): List<OnflixStream> {
        val flight = extractFlight(html)
        val json = extractEpisodesArray(flight) ?: return emptyList()
        return tryParseJson<List<OnflixStream>>(json).orEmpty()
    }

    /** Extracts the `"episodes":[ ... ]` array from clean (un-escaped) flight JSON. */
    private fun extractEpisodesArray(flight: String): String? {
        val key = "\"episodes\":["
        val keyIndex = flight.indexOf(key)
        if (keyIndex < 0) return null

        val start = keyIndex + key.length - 1 // index of '['
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

    private fun OnflixMovie.toSearchResponse(horizontal: Boolean = false): SearchResponse? {
        val slug = this.slug ?: return null
        val title = this.title ?: this.originalTitle ?: return null
        val image = if (horizontal) posterUrl ?: thumbUrl else thumbUrl ?: posterUrl
        return newMovieSearchResponse(title, "$mainUrl/phim/$slug", TvType.Movie) {
            this.posterUrl = image
        }
    }

    @Keep
    data class OnflixListResponse(
        @JsonProperty("data") val data: List<OnflixMovie> = emptyList(),
    )

    @Keep
    data class OnflixSearchResponse(
        @JsonProperty("movies") val movies: List<OnflixMovie> = emptyList(),
    )

    @Keep
    data class OnflixMovie(
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("original_title") val originalTitle: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("poster_url") val posterUrl: String? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("type") val type: String? = null,
    )

    @Keep
    data class OnflixStream(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("server_name") val serverName: String? = null,
        @JsonProperty("link_m3u8") val linkM3u8: String? = null,
        @JsonProperty("link_embed") val linkEmbed: String? = null,
        @JsonProperty("type") val type: String? = null,
    )
}
