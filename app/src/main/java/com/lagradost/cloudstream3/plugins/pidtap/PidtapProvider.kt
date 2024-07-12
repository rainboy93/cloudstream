package com.lagradost.cloudstream3.plugins.pidtap

import android.net.Uri
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class PidtapProvider(val plugin: PidtapPlugin) : MainAPI() {

    override var mainUrl = "https://pidtap.github.io/phimle"
    override var name = "Pidtap"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/chautinhtri/" to "CHÂU TINH TRÌ",
        "$mainUrl/lylienkiet/" to "LÝ LIÊN KIỆT",
        "$mainUrl/thanhlong/" to "THÀNH LONG",
        "$mainUrl/chungtudon/" to "CHÂN TỬ ĐAN",
        "$mainUrl/lamchanhanh/" to "LÂM CHÁNH ANH",
        "$mainUrl/tonghop/" to "TỔNG HỢP"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        if (page == 1) {
            val document = app.get("${request.data}tonghop.html").document
            val homeItems = document.selectFirst(Evaluator.Class("allfilm"))
                ?.select(Evaluator.Tag("a"))
                ?.mapNotNull {
                    it.toSearchResponse(request.data)
                } ?: listOf()
            return newHomePageResponse(request.name, homeItems)
        } else {
            return super.getMainPage(page, request)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val main = document.selectFirst(Evaluator.Class("main")) ?: return super.load(url)
        val title = main.selectFirst(Evaluator.Class("tieude"))?.text() ?: ""
        val parts = main.selectFirst(Evaluator.Class("thap"))
        val links = main.selectFirst(Evaluator.Class("link"))
        val tvType = if (links != null) TvType.TvSeries else TvType.Movie
        return if (tvType == TvType.TvSeries) {
            var numberOfEp = links!!.select(Evaluator.Tag("a")).size
            if (parts != null) {
                try {
                    numberOfEp =
                        parts.select(Evaluator.Tag("p")).last()!!.text().split("-").last().trim()
                            .toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val episodes = (1..numberOfEp).map {
                val href = url.replace("1", "$it")
                val name = "Episode $it"
                Episode(
                    data = href,
                    name = name,
                    episode = it,
                )
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) { }
        } else {
            val link = main.selectFirst(Evaluator.Tag("iframe"))?.attr("src") ?: ""
            newMovieLoadResponse(title, url, TvType.Movie, link) { }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        var document = app.get(fixUrl(data)).document
        if (data.contains(mainUrl)) {
            val main = document.selectFirst(Evaluator.Class("main"))
                ?: return super.loadLinks(data, isCasting, subtitleCallback, callback)
            val link = main.selectFirst(Evaluator.Tag("iframe"))?.attr("src") ?: ""
            document = app.get(fixUrl(link)).document
            processData(link, document, callback)
        } else {
            processData(data, document, callback)
        }
        return true
    }

    private suspend fun processData(
        data: String,
        document: Document,
        callback: (ExtractorLink) -> Unit
    ) {
        val uri = Uri.parse(data)
        val url = "${uri.scheme}://${uri.host}"
        val path = document.data().substringAfter("file: '").substringBefore("'")
        safeApiCall {
            callback.invoke(
                ExtractorLink(
                    "Source",
                    "Source",
                    "$url$path",
                    referer = url,
                    quality = Qualities.P1080.value,
                    isM3u8 = true
                )
            )
        }
    }

    private fun Element.toSearchResponse(url: String): LiveSearchResponse? {
        val link = attr("href") ?: return null
        val name = selectFirst(Evaluator.Tag("h5"))?.text() ?: ""
        val img = selectFirst("img")?.attr("src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = "$url$link",
            posterUrl = fixUrl(img.trim().replace("..", "")),
            type = TvType.Movie,
            apiName = "Pidtap"
        )
    }
}