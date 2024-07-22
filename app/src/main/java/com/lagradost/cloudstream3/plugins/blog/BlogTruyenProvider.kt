package com.lagradost.cloudstream3.plugins.blog

import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPage
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class BlogTruyenProvider : MainAPI() {

    override var mainUrl = "https://blogtruyen.vn"
    override var name = "Blog Truyện"
    override val supportedTypes = setOf(TvType.Manga)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        mainPage(mainUrl, "Top"),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val document = if (request.name == "Top") {
            if (page == 1) {
                app.get(request.data).document
            } else {
                null
            }
        } else {
            app.get(request.data + page).document
        }
        if (document == null) return super.getMainPage(page, request)
        val homeItems = document
            .selectFirst(Evaluator.Id("tabs-top-all"))
            ?.select(Evaluator.Tag("p"))
            ?.mapNotNull {
                it.toSearchResponse()
            } ?: listOf()
        return newHomePageResponse(request, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val posterUrl = document.selectFirst(Evaluator.AttributeWithValue("property", "og:image"))
            ?.attr("content")
            ?: ""
        val title = document.selectFirst(Evaluator.AttributeWithValue("property", "og:title"))
            ?.attr("content")
            ?.split("|")
            ?.firstOrNull()
            ?.trim()
            ?: ""
        val description = document.selectFirst(Evaluator.Class("content"))
            ?.text() ?: ""
        val episodes = document.selectFirst(Evaluator.Id("list-chapters"))
            ?.select(Evaluator.Tag("p"))
            ?.map {
                val a = it.selectFirst(Evaluator.Tag("a"))
                val href = a?.attr("href") ?: ""
                val name = a?.text() ?: ""
                val desc = it.selectFirst(Evaluator.Class("publishedDate"))
                    ?.text()?.trim() ?: ""
                Episode(
                    data = href,
                    name = name,
                    description = desc
                )
            }?.reversed() ?: listOf()
        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = fixUrl(posterUrl)
            this.plot = description
        }
    }

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val link = selectFirst(Evaluator.Tag("a")) ?: return null
        val name = link.attr("title") ?: ""
        val href = fixUrl(link.attr("href"))
        return LiveSearchResponse(
            name = name,
            url = href,
            type = TvType.Manga,
            apiName = "Blog Truyện"
        )
    }
}