package com.lagradost.cloudstream3.plugins.bluphim

import com.blankj.utilcode.util.LogUtils
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class BluPhimProvider(val plugin: BluPhimPlugin) : MainAPI() {
    override var mainUrl = "https://bluphim.net/"
    override var name = "Blu phim"
    override val supportedTypes = setOf(TvType.Movie)

    override var lang = "vi"

    // enable this when your provider has a main page
    override val hasMainPage = true

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

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val name = selectFirst(Evaluator.Class("label"))?.text() ?: ""
        val link = selectFirst(Evaluator.Attribute("href")) ?: return null
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