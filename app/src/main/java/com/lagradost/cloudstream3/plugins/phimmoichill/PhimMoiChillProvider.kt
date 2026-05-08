package com.lagradost.cloudstream3.plugins.phimmoichill

import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
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
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Element

class PhimMoiChillProvider(val plugin: PhimMoiChillPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "PhimMoiChill"
    override var mainUrl = "https://phimmoichill.ai"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    override val mainPage = mainPageOf(
        Pair("$mainUrl/list/phim-le", "Phim Lẻ/horizontal"),
        Pair("$mainUrl/list/phim-bo", "Phim Bộ/vertical"),
        Pair("$mainUrl/genre/phim-chieu-rap", "Phim Chiếu Rạp/horizontal"),
        Pair("$mainUrl/list/phim-hot", "Phim Hot/vertical"),
        Pair("$mainUrl/country/phim-han-quoc", "Phim Hàn Quốc/vertical"),
        Pair("$mainUrl/country/phim-trung-quoc", "Phim Trung Quốc/vertical"),
        Pair("$mainUrl/country/phim-au-my", "Phim Âu Mỹ/vertical"),
    )

    companion object {
        private const val IMAGE_BASE = "https://img.phimmoichill.ai/images/info"
        private const val PLAYER_URL = "https://sotrim.listpm.net/mpeg"
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val a = selectFirst("a") ?: return null
        val href = a.attr("href")
        val title = a.attr("title").ifEmpty {
            selectFirst("h3")?.text() ?: a.selectFirst("p")?.text() ?: return null
        }
        val posterUrl = selectFirst("img")?.attr("src")
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val name = request.name.split("/")[0]
        val horizontal = request.name.split("/")[1] == "horizontal"

        val url = if (page <= 1) "${request.data}/" else "${request.data}/page-$page/"
        val doc = app.get(url).document

        val items = doc.select("ul.list-film li.item").mapNotNull { it.toSearchResponse() }

        val homePageList = HomePageList(
            name = name,
            list = items,
            isHorizontalImages = horizontal
        )

        return newHomePageResponse(list = homePageList, hasNext = items.isNotEmpty())
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/tim-kiem/$query/").document
        return doc.select("ul.list-film li.item").mapNotNull { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst("h1[itemprop=name]")?.text()?.trim() ?: ""
        val posterUrl = doc.selectFirst("img.avatar")?.attr("src")
        val description = doc.selectFirst("div#film-content")?.text()?.trim()

        val metaItems = doc.select("ul.entry-meta.block-film li")
        var year: Int? = null
        var duration: Int? = null
        val tags = mutableListOf<String>()
        val actors = mutableListOf<String>()

        for (li in metaItems) {
            val label = li.selectFirst("label")?.text()?.trim() ?: continue
            when {
                label.contains("Năm") -> {
                    year = li.select("a").firstOrNull()?.text()?.trim()?.toIntOrNull()
                }

                label.contains("Thể loại") -> {
                    tags.addAll(li.select("a").map { it.text().trim() })
                }

                label.contains("Diễn viên") -> {
                    actors.addAll(li.select("a").map { it.text().trim() })
                }

                label.contains("Thời lượng") -> {
                    val text = li.text().replace(label, "").trim()
                    duration =
                        Regex("(\\d+)\\s*[Pp]hút").find(text)?.groupValues?.get(1)?.toIntOrNull()
                }
            }
        }

        val watchUrl = doc.selectFirst("a.btn-see")?.attr("href")
            ?: doc.selectFirst("div.latest-episode a")?.attr("href")

        val isSingleMovie = metaItems.any { li ->
            val label = li.selectFirst("label")?.text() ?: ""
            val value = li.text().replace(label, "").trim()
            label.contains("Thời lượng") && value.contains("Phút", ignoreCase = true)
        }

        if (isSingleMovie || watchUrl?.contains("tap-full") == true) {
            return newMovieLoadResponse(title, url, TvType.Movie, watchUrl ?: "") {
                this.posterUrl = posterUrl
                this.plot = description
                this.year = year
                this.tags = tags
                this.duration = duration
                addActors(actors.map { com.lagradost.cloudstream3.Actor(it, "") })
            }
        }

        val episodes = if (watchUrl != null) {
            loadEpisodesFromWatchPage(watchUrl)
        } else {
            doc.select("div.latest-episode a").mapNotNull { a ->
                val href = a.attr("href")
                val epName = a.text().trim()
                newEpisode(href) {
                    name = epName
                }
            }.reversed()
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = description
            this.year = year
            this.tags = tags
            addActors(actors.map { com.lagradost.cloudstream3.Actor(it, "") })
        }
    }

    private suspend fun loadEpisodesFromWatchPage(watchUrl: String): List<Episode> {
        val doc = app.get(watchUrl).document
        return doc.select("div#list-server ul.episodes a").mapNotNull { a ->
            val href = a.attr("href")
            val epName = a.text().trim()
            val epNum = Regex("(\\d+)").find(epName)?.groupValues?.get(1)?.toIntOrNull()
            newEpisode(href) {
                name = epName
                episode = epNum
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        if (data.isBlank()) return false

        val watchDoc = app.get(data).document
        val episodeId = Regex("filmInfo\\.episodeID\\s*=\\s*parseInt\\('(\\d+)'\\)")
            .find(watchDoc.html())?.groupValues?.get(1) ?: return false

        val playerResponse = app.post(
            "$mainUrl/chillsplayer.php",
            data = mapOf("qcao" to episodeId),
            referer = data,
            headers = mapOf("X-Requested-With" to "XMLHttpRequest")
        ).text

        val hash = Regex("iniPlayers\\(\"([a-f0-9]+)\"").find(playerResponse)?.groupValues?.get(1)

        if (hash != null) {
            val m3u8Url = "$PLAYER_URL/$hash/index.m3u8"
            callback.invoke(
                newExtractorLink(
                    source = name,
                    url = m3u8Url,
                    name = name,
                    initializer = {
                        referer = mainUrl
                        quality = Qualities.Unknown.value
                        type = ExtractorLinkType.M3U8
                    }
                )
            )
        }

        Regex("file:\\s*\"([^\"]+\\.srt)\"[^}]*label:\\s*\"([^\"]+)\"").findAll(playerResponse)
            .forEach { match ->
                subtitleCallback.invoke(
                    SubtitleFile(
                        lang = match.groupValues[2],
                        url = match.groupValues[1]
                    )
                )
            }

        return hash != null
    }
}
