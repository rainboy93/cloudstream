package com.lagradost.cloudstream3.plugins.nguonc

import android.net.Uri
import com.blankj.utilcode.util.LogUtils
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
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
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class NguonCProvider : MainAPI() {

    override var mainUrl = "https://phim.nguonc.com"
    override var name = "NguonC"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/danh-sach/phim-le?page=" to "Phim Lẻ",
        "$mainUrl/danh-sach/phim-bo?page=" to "Phim ",
        "$mainUrl/quoc-gia/han-quoc?page=" to "Phim Hàn Quốc",
        "$mainUrl/quoc-gia/trung-quoc?page=" to "Phim Trung Quốc",
        "$mainUrl/quoc-gia/au-my?page=" to "Phim Âu Mỹ",
        "$mainUrl/quoc-gia/viet-nam?page=" to "Phim Việt Nam",
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}/tim-kiem?keyword=$query")
            .document
            .select(Evaluator.Tag("tr"))
            .mapNotNull {
                it.toSearchResponse()
            }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val homeItems = document.select(Evaluator.Tag("tr"))
            .mapNotNull {
                it.toSearchResponse()
            }
        return newHomePageResponse(request.name, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val info = document.selectFirst(Evaluator.Class("flex flex-col")) ?: return null
        val img = info.selectFirst(Evaluator.Tag("img"))
        val posterUrl = img?.attr("data-src") ?: ""
        val title = document.selectFirst(Evaluator.Class("text-center rounded-md"))
            ?.selectFirst(Evaluator.Tag("h1"))
            ?.text() ?: ""
        var year = 2024
        var actors = listOf<String>()
        info.selectFirst(Evaluator.Tag("tbody"))?.select(Evaluator.Tag("tr"))?.forEach {
            val tdFirst = it.selectFirst(Evaluator.Tag("td"))?.text()?.trim() ?: ""
            val tdLast = it.select(Evaluator.Tag("td")).lastOrNull()?.text()?.trim() ?: ""
            when {
                tdFirst.contains("Năm") -> {
                    year = tdLast.toIntOrNull() ?: 2024
                }

                tdFirst.contains("Diễn Viên") -> {
                    actors = listOf(tdFirst)
                }
            }
        }
        val links = info.selectFirst(Evaluator.Class("w-full px-2 sm:px-0"))
            ?.select(Evaluator.AttributeWithValue("target", "_blank")) ?: listOf()
        val rating = document.select(Evaluator.Class("film-status")).lastOrNull()
            ?.select("a")?.text()
        val description =
            info.selectFirst(Evaluator.Tag("article"))?.selectFirst(Evaluator.Tag("p"))?.text()
                ?: ""
        val tvType = if (links.size > 1) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
        return if (tvType == TvType.TvSeries) {
            val episodes = links.map {
                val href = it.attr("href")
                val episode = it.text().replace(Regex("[^0-9]"), "").trim().toIntOrNull()
                val name = "Episode $episode"
                Episode(
                    data = href,
                    name = name,
                    episode = episode,
                    posterUrl = posterUrl
                )
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = fixUrl(posterUrl)
                this.year = year
                this.plot = description
                this.rating = rating?.toIntOrNull()
                addActors(actors)
            }
        } else {
            val link = links.firstOrNull()?.attr("href") ?: ""
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = fixUrl(posterUrl)
                this.year = year
                this.plot = description
                this.rating = rating?.toIntOrNull()
                addActors(actors)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val content = document.data()
        LogUtils.d(content)
        val path = content.substringAfter("file\":\"").substringBefore("\",")
        val uri = Uri.parse(data)
        safeApiCall {
            callback.invoke(
                ExtractorLink(
                    "Embed",
                    "Embed",
                    "${uri.scheme}://${uri.host}${path}",
                    referer = data,
                    quality = Qualities.P1080.value,
                    isM3u8 = true
                )
            )
        }
        return super.loadLinks(data, isCasting, subtitleCallback, callback)
    }

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val link = selectFirst(Evaluator.Tag("a")) ?: return null
        val name = link.attr("title")
        val href = fixUrl(link.attr("href") ?: "")
        val img = selectFirst("img")?.attr("data-src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = href,
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "NguonC"
        )
    }
}