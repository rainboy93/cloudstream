package com.lagradost.cloudstream3.plugins.bluphim

import com.blankj.utilcode.util.LogUtils
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class BluPhimProvider(val plugin: BluPhimPlugin) : MainAPI() {
    override var mainUrl = "https://bluphim.net"
    override var name = "Blu phim"
    override val supportedTypes = setOf(TvType.Movie)

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
        val poster = document.selectFirst("poster") ?: return null
        val img = poster.selectFirst(Evaluator.Class("adspruce-streamlink"))?.selectFirst("img")
        val posterUrl = img?.attr("src")
        val title = img?.attr("title")
        val youtubeTrailer =
            poster.selectFirst("btn-see btn btn-primary btn-download-link")?.attr("href")
        val rating = document.select(Evaluator.Class("film-status")).lastOrNull()
            ?.select("a")?.text()
        val details = document.selectFirst(Evaluator.Class("detail"))

        return super.load(url)
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