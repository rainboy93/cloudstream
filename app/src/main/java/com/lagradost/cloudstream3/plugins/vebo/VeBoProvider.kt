package com.lagradost.cloudstream3.plugins.vebo

import com.google.gson.Gson
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class VeBoProvider(val plugin: VeBoPlugin) : MainAPI() {

    private val gson by lazy { Gson() }

    override var mainUrl = ""
    override var name = "VeBo TV"
    override val supportedTypes = setOf(TvType.Movie, TvType.Live)

    override var lang = "vi"

    override val hasMainPage = true
    override val hasDownloadSupport = false

    override val mainPage: List<MainPageData> = mainPageOf(
        "https://api.vebo.xyz/api/news/vebotv/list/xemlai/" to "Xem lại",
        "https://api.vebo.xyz/api/news/vebotv/list/highlight/" to "Highlights"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val text = app.get(request.data + page).text
        try {
            val response = gson.fromJson(text, WatchResponse::class.java)
            val list = response.data.list.toMutableList()
            if (response.data.highlight != null) {
                list.add(0, response.data.highlight)
            }
            val matches = list.map {
                it.toSearchResponse()
            }
            return newHomePageResponse(request.name, matches)
        } catch (e: Exception) {
            return super.getMainPage(page, request)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val text = app.get(url).text
        try {
            val response = gson.fromJson(text, WatchDetailResponse::class.java)
            return newMovieLoadResponse(
                response.data?.name ?: "",
                url,
                TvType.Movie,
                response.data?.videoUrl ?: ""
            ) {
                this.posterUrl = response.data?.featureImage
                this.plot = response.data?.description
                this.recommendations = response.dataRelated?.filterNotNull()?.map {
                    it.toSearchResponse()
                }
            }
        } catch (e: Exception) {
            return super.load(url)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        safeApiCall {
            callback.invoke(
                ExtractorLink(
                    "Live",
                    "Live",
                    data,
                    referer = "",
                    quality = Qualities.P1080.value,
                    isM3u8 = true
                )
            )
        }
        return true
    }

    private fun WatchResponse.Data.Match.toSearchResponse(): LiveSearchResponse {
        return LiveSearchResponse(
            name = this.name ?: "",
            url = "https://api.vebo.xyz/api/news/vebotv/detail/${id}",
            posterUrl = this.featureImage ?: "",
            type = TvType.Movie,
            apiName = "VeBo TV"
        )
    }

    private fun WatchDetailResponse.Data.toSearchResponse(): SearchResponse {
        return LiveSearchResponse(
            name = this.name ?: "",
            url = "https://api.vebo.xyz/api/news/vebotv/detail/${id}",
            posterUrl = this.featureImage ?: "",
            type = TvType.Movie,
            apiName = "VeBo TV"
        )
    }
}