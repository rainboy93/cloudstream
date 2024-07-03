package com.lagradost.cloudstream3.plugins.bluphim

import com.blankj.utilcode.util.LogUtils
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class BluPhimProvider(val plugin: BluPhimPlugin) : MainAPI() {
    override var mainUrl = "https://bluphim.net"
    override var name = "Blu phim"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        MainPageData("PHIM MỚI", "phim-moi", false),
        MainPageData("PHIM BỘ", "phim-bo", false),
        MainPageData("PHIM LẺ", "phim-le", false),
        MainPageData("PHIM CHIẾU RẠP", "phim-chieu-rap", false)

    )

    // this function gets called when you search for something
    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}search?k=$query")
            .document
            .getElementsByClass("list-films film-new")
            .firstOrNull()
            ?.select(Evaluator.AttributeWithValueStarting("class", "item"))
            ?.mapNotNull {
                it.toSearchResponse().also { response ->
                    LogUtils.d(response)
                }
            } ?: listOf()
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val homeItems = app.get("${mainUrl}/the-loai/${request.data}-$page")
            .document
            .getElementsByClass("list-films film-new")
            .firstOrNull()
            ?.select(Evaluator.AttributeWithValueStarting("class", "item"))
            ?.mapNotNull {
                it.toSearchResponse().also { response ->
                    LogUtils.d(response)
                }
            } ?: listOf()
        return newHomePageResponse(request.name, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val poster = document.selectFirst(Evaluator.Class("poster")) ?: return null
        val img = poster.selectFirst(Evaluator.Class("adspruce-streamlink"))
            ?.selectFirst(Evaluator.Tag("img"))
        val posterUrl = img?.attr("src") ?: ""
        val title = img?.attr("title") ?: ""
        val youtubeTrailer =
            poster.selectFirst(Evaluator.Class("btn-see btn btn-primary btn-download-link"))
                ?.attr("href") ?: ""
        val link = poster.selectFirst(Evaluator.Class("btn-see btn btn-danger btn-stream-link"))
            ?.attr("href") ?: ""
        val rating = document.select(Evaluator.Class("film-status")).lastOrNull()
            ?.select("a")?.text()
        val details = document.selectFirst(Evaluator.Class("detail"))
        val description =
            details?.selectFirst(Evaluator.Id("info-film"))?.selectFirst(Evaluator.Class("tab"))
                ?.selectFirst(Evaluator.Tag("p"))?.text()
        val tvType = if (document.select(Evaluator.ContainsText("TV Series")).isNotEmpty()) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
        val actors =
            poster.selectFirst(Evaluator.Class("dienviendd"))?.allElements?.map { it.text() }
        val tags = poster.selectFirst(Evaluator.Class("theloaidd"))?.allElements?.map { it.text() }
        val year = poster.selectFirst(Evaluator.Class("dinfo"))?.run {

        }
        return if (tvType == TvType.TvSeries) {
            val docEpisodes = app.get(fixUrl(link)).document
            val episodes = docEpisodes.select(Evaluator.Class("list-episode")).lastOrNull()
                ?.select(Evaluator.Tag("a"))?.map {
                    val href = it.attr("href")
                    val episode = it.text().replace(Regex("[^0-9]"), "").trim().toIntOrNull()
                    val name = "Episode $episode"
                    Episode(
                        data = href,
                        name = name,
                        episode = episode,
                    )
                } ?: listOf()
            val newTvSeriesLoadResponse =
                newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                    this.posterUrl = fixUrl(posterUrl)
//                this.year = year
                    this.plot = description
                    this.tags = tags
                    this.rating = rating?.toIntOrNull()
                    addActors(actors)
//                this.recommendations = recommendations
                    addTrailer(youtubeTrailer)
                }
            newTvSeriesLoadResponse
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = fixUrl(posterUrl)
//                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating?.toIntOrNull()
                addActors(actors)
//                this.recommendations = recommendations
                addTrailer(youtubeTrailer)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return super.loadLinks(data, isCasting, subtitleCallback, callback)
    }

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val link = selectFirst(Evaluator.Attribute("href")) ?: return null
        val name = link.selectFirst(Evaluator.Class("name"))?.selectFirst("span")?.text() ?: ""
        val href = fixUrl(link.attr("href"))
        val img = link.selectFirst("img")?.attr("src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = href,
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "Blu phim"
        )
    }
}