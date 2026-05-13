package com.lagradost.cloudstream3.plugins.onflix

import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addPoster
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class OnflixProvider(val plugin: OnflixPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "Onflix"
    override var mainUrl = "https://onflixtv.com"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    override val mainPage = mainPageOf(
        Pair("${mainUrl}/the-loai/phim-moi", "Phim Mới/vertical"),
        Pair("${mainUrl}/the-loai/phim-le", "Phim Lẻ/horizontal"),
        Pair("${mainUrl}/the-loai/phim-bo", "Phim Bộ/vertical"),
        Pair("${mainUrl}/the-loai/phim-chieu-rap", "Phim Chiếu Rạp/horizontal"),
        Pair("${mainUrl}/the-loai/hanh-dong", "Hành Động/vertical"),
        Pair("${mainUrl}/the-loai/hoat-hinh", "Hoạt Hình/vertical"),
        Pair("${mainUrl}/quoc-gia/han-quoc", "Phim Hàn Quốc/vertical"),
        Pair("${mainUrl}/quoc-gia/au-my", "Phim Âu - Mỹ/vertical"),
    )

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("${mainUrl}/tim-kiem?keyword=${query}").document
        return doc.select("div.movie-card").mapNotNull { it.toSearchResponse() }
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val categoryName = request.name.split("/")[0]
        val horizontal = request.name.split("/")[1] == "horizontal"
        val url = "${request.data}-${page}"
        val doc = app.get(url).document
        val list = doc.select("div.movie-card").mapNotNull { it.toSearchResponse(horizontal) }
        val hasNext = doc.select("ul.pagination li:not(.active)").isNotEmpty()

        return newHomePageResponse(
            list = HomePageList(
                name = categoryName,
                list = list,
                isHorizontalImages = horizontal
            ),
            hasNext = hasNext
        )
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst("div.movie-details h2")?.text()?.trim()
            ?: doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim() ?: ""

        val description = doc.selectFirst("p:has(strong:containsOwn(Mô tả))")?.text()
            ?.removePrefix("Mô tả:")?.trim()
            ?: doc.selectFirst("meta[property=og:description]")?.attr("content")?.trim()

        val posterUrl = doc.selectFirst("img.thumb-overlay")?.attr("src")?.let { fixUrl(it) }
            ?: doc.selectFirst("meta[property=og:image]")?.attr("content")

        val year = doc.selectFirst("p:has(strong:containsOwn(Năm))")?.text()
            ?.replace(Regex("[^0-9]"), "")?.toIntOrNull()

        val genres = doc.select("p:has(strong:containsOwn(Thể loại)) a").map { it.text().trim() }

        val actors = doc.select("p:has(strong:containsOwn(Diễn viên)) a[href*=/dien-vien/]")
            .map { it.text().trim() }.filter { it.isNotEmpty() }

        val ratingText = doc.selectFirst("#ratingValue")?.text()?.trim()
        val ratingValue = ratingText?.toDoubleOrNull()

        val trailerUrl = Regex(""""url"\s*:\s*"(https://www\.youtube\.com/watch[^"]*?)"""")
            .find(doc.html())?.groupValues?.get(1)

        val watchUrl = url.replace("/phim/", "/xem-phim/")

        val watchDoc = app.get(watchUrl).document
        val episodes = parseEpisodes(watchDoc)

        val isSeries = genres.any { it.contains("Phim bộ") || it.contains("TV Series") }
                || episodes.size > 1

        if (isSeries) {
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.plot = description
                this.year = year
                this.tags = genres
                this.score = Score.from(ratingValue, 10)
                addPoster(posterUrl)
                addActors(actors.map { Actor(it, "") })
                if (trailerUrl != null) addTrailer(trailerUrl)
            }
        }

        val dataUrl = if (episodes.isNotEmpty()) episodes[0].data else watchUrl

        return newMovieLoadResponse(title, url, TvType.Movie, dataUrl) {
            this.plot = description
            this.year = year
            this.tags = genres
            this.score = Score.from(ratingValue, 10)
            addPoster(posterUrl)
            addActors(actors.map { Actor(it, "") })
            if (trailerUrl != null) addTrailer(trailerUrl)
        }
    }

    private fun parseEpisodes(watchDoc: Document): List<com.lagradost.cloudstream3.Episode> {
        return watchDoc.select("div.episode-item button[onclick]").mapNotNull { btn ->
            val onclick = btn.attr("onclick")
            val epUrl = Regex("""'([^']*)'""").find(onclick)?.groupValues?.get(1)
                ?: return@mapNotNull null
            val epName = btn.attr("title").ifEmpty { btn.text().trim() }
            newEpisode(fixUrl(epUrl)) {
                this.name = epName
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val watchDoc = app.get(data).document
        val iframeSrc = watchDoc.selectFirst("iframe#video-iframe")?.attr("src")
            ?: return false

        val decodedSrc = iframeSrc.replace("&amp;", "&")

        try {
            val embedPage = app.get(decodedSrc, referer = mainUrl).text
            val streamUrl = Regex("""src='([^']*streaming[^']*)'""")
                .find(embedPage)?.groupValues?.get(1)
                ?: Regex("""src="([^"]*streaming[^"]*)" """)
                    .find(embedPage)?.groupValues?.get(1)

            if (streamUrl != null) {
                val streamPage = app.get(streamUrl, referer = decodedSrc).text
                val m3u8 = Regex("""var url\s*=\s*'([^']+\.m3u8[^']*)'""")
                    .find(streamPage)?.groupValues?.get(1)

                if (m3u8 != null) {
                    callback.invoke(
                        newExtractorLink(
                            source = name,
                            url = m3u8,
                            name = "$name - HLS",
                        ) {
                            referer = streamUrl
                            quality = Qualities.Unknown.value
                            type = ExtractorLinkType.M3U8
                        }
                    )
                    return true
                }
            }
        } catch (_: Exception) {
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                url = decodedSrc,
                name = "$name - Embed",
            ) {
                referer = mainUrl
                quality = Qualities.Unknown.value
                type = ExtractorLinkType.VIDEO
            }
        )

        return true
    }

    private fun Element.toSearchResponse(horizontal: Boolean = false): SearchResponse? {
        val a = this.selectFirst("a[href*=/phim/]") ?: return null
        val href = fixUrl(a.attr("href"))
        val title = a.attr("title").removePrefix("Phim ").removePrefix("Xem phim ").trim()
            .ifEmpty { a.selectFirst("img")?.attr("alt")?.trim() ?: return null }
        val img = a.selectFirst("img:not(.hot-icon)")
            ?.let { it.attr("src").ifEmpty { it.attr("data-src") } } ?: ""
        val posterUrl = fixUrl(img)

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    private fun fixUrl(url: String): String {
        if (url.startsWith("http")) return url
        return "${mainUrl}${url}"
    }
}
