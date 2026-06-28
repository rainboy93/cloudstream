package com.lagradost.cloudstream3.plugins.motchill

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Document
import java.net.URLEncoder

class MotChillProvider(val plugin: MotChillPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "MotChill"
    override var mainUrl = "https://motchille.ac"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    override val mainPage = mainPageOf(
        Pair("$mainUrl/danh-sach/phim-le", "Phim Lẻ"),
        Pair("$mainUrl/danh-sach/phim-bo", "Phim Bộ"),
        Pair("$mainUrl/danh-sach/phim-chieu-rap", "Phim Chiếu Rạp"),
        Pair("$mainUrl/danh-sach/phim-thuyet-minh", "Phim Thuyết Minh"),
        Pair("$mainUrl/the-loai/hoat-hinh", "Anime & Hoạt Hình"),
        Pair("$mainUrl/quoc-gia/han-quoc", "Phim Hàn Quốc"),
        Pair("$mainUrl/quoc-gia/trung-quoc", "Phim Trung Quốc"),
        Pair("$mainUrl/quoc-gia/au-my", "Phim Âu Mỹ"),
    )

    private fun fixUrl(url: String): String {
        if (url.isBlank()) return url
        if (url.startsWith("http")) return url
        return if (url.startsWith("/")) "$mainUrl$url" else "$mainUrl/$url"
    }

    private fun parseCards(doc: Document): List<SearchResponse> {
        return doc.select("a[href^=/phim/]")
            .filter { it.attr("href").trim('/').count { c -> c == '/' } == 1 }
            .mapNotNull { a ->
                val href = a.attr("href")
                val img = a.selectFirst("img") ?: return@mapNotNull null
                val title = a.attr("title").ifBlank { img.attr("alt") }.ifBlank {
                    return@mapNotNull null
                }
                val poster = img.attr("src").ifBlank { img.attr("data-src") }
                newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                    this.posterUrl = fixUrl(poster)
                }
            }
            .distinctBy { it.url }
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val url = if (page > 1) "${request.data}/$page" else request.data
        val items = parseCards(app.get(url).document)

        return newHomePageResponse(
            list = HomePageList(name = request.name, list = items, isHorizontalImages = false),
            hasNext = items.isNotEmpty()
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return parseCards(app.get("$mainUrl/search?q=$encoded").document)
    }

    private fun regex(pattern: String, input: String): String? {
        return Regex(pattern).find(input)?.groupValues?.getOrNull(1)
    }

    private fun unescape(value: String): String {
        return value
            .replace("\\\"", "\"")
            .replace("\\/", "/")
            .replace("\\\\", "\\")
    }

    private fun extractEpisodes(text: String): List<EpisodeItem> {
        val anchor = text.indexOf("Danh sách tập phim")
        val from = if (anchor >= 0) anchor else 0
        val key = text.indexOf("episodes\\\":[", from)
        if (key < 0) return emptyList()
        val open = text.indexOf('[', key)
        val close = text.indexOf(']', open)
        if (open < 0 || close < 0) return emptyList()
        val json = text.substring(open, close + 1).replace("\\\"", "\"")
        return tryParseJson<List<EpisodeItem>>(json) ?: emptyList()
    }

    override suspend fun load(url: String): LoadResponse {
        val text = app.get(url).text

        val title = regex(
            "\"@type\":\"(?:Movie|TVSeries)\",\"name\":\"((?:[^\"\\\\]|\\\\.)*)\"",
            text
        )?.let { unescape(it) } ?: "Motchill"
        val poster = regex("property=\"og:image\" content=\"([^\"]+)\"", text)
        val year = regex("\"datePublished\":\"(\\d{4})", text)?.toIntOrNull()
        val plot = regex(
            "\"description\":\"((?:[^\"\\\\]|\\\\.)*)\",\"datePublished\"",
            text
        )?.let { unescape(it) }
        val tags = regex("\"genre\":\\[([^\\]]*)\\]", text)
            ?.let { Regex("\"([^\"]+)\"").findAll(it).map { m -> m.groupValues[1] }.toList() }
            ?: emptyList()

        // The episode list mixes several servers; the same episode appears under
        // each server with a different slug and name format ("1" vs "Tập 01").
        // Group by episode number so servers merge into a single entry, and sort
        // numerically (1, 2, ... 10) instead of lexicographically.
        val groups = LinkedHashMap<String, MutableList<EpisodeItem>>()
        extractEpisodes(text).forEach { ep ->
            groups.getOrPut(episodeKey(ep)) { mutableListOf() }.add(ep)
        }
        val sorted = groups.entries.sortedWith(
            compareBy({ it.key.toIntOrNull() == null }, { it.key.toIntOrNull() ?: 0 }, { it.key })
        )

        val isSeries = text.contains("\"@type\":\"TVSeries\"") || sorted.size > 1

        if (isSeries) {
            val eps = sorted.map { (key, items) ->
                val number = key.toIntOrNull()
                newEpisode("$url@@@$key") {
                    this.name = number?.let { "Tập $it" } ?: "Tập ${items.first().name}"
                    this.episode = number
                    this.posterUrl = poster
                }
            }
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, eps) {
                this.plot = plot
                this.year = year
                this.tags = tags
                this.posterUrl = poster
            }
        }

        val firstKey = sorted.firstOrNull()?.key.orEmpty()
        return newMovieLoadResponse(title, url, TvType.Movie, "$url@@@$firstKey") {
            this.plot = plot
            this.year = year
            this.tags = tags
            this.posterUrl = poster
        }
    }

    // A stable per-episode key: the episode number when present (so different
    // servers/name formats collapse together), otherwise the raw slug.
    private fun episodeKey(ep: EpisodeItem): String {
        return Regex("\\d+").find(ep.name)?.value?.toIntOrNull()?.toString() ?: ep.slug
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val url = data.substringBefore("@@@")
        val key = data.substringAfter("@@@", "")

        val text = app.get(url).text
        val links = extractEpisodes(text)
            .filter { (key.isEmpty() || episodeKey(it) == key) && it.type == "m3u8" && it.link.isNotBlank() }

        links.forEach { ep ->
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = ep.server.ifBlank { name },
                    url = ep.link,
                    type = ExtractorLinkType.M3U8,
                    initializer = {
                        referer = "$mainUrl/"
                        quality = Qualities.Unknown.value
                    }
                )
            )
        }

        return links.isNotEmpty()
    }

    data class EpisodeItem(
        @JsonProperty("server") val server: String = "",
        @JsonProperty("name") val name: String = "",
        @JsonProperty("slug") val slug: String = "",
        @JsonProperty("type") val type: String = "",
        @JsonProperty("link") val link: String = "",
    )
}
