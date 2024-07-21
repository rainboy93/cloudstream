package com.lagradost.cloudstream3.plugins.ophim

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
import com.lagradost.cloudstream3.ShowStatus
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.plugins.toInteger
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class OPhimProvider : MainAPI() {

    private var subUrl = ""
    private var imgUrl = ""

    override var mainUrl = "https://ophim1.com/v1/api"

    override var name = "Ổ Phim"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/danh-sach/phim-le?page=" to "Phim Lẻ",
        "$mainUrl/danh-sach/phim-bo?page=" to "Phim Bộ",
        "$mainUrl/quoc-gia/han-quoc?page=" to "Phim Hàn Quốc",
        "$mainUrl/quoc-gia/trung-quoc?page=" to "Phim Trung Quốc",
        "$mainUrl/quoc-gia/au-my?page=" to "Phim Âu Mỹ",
        "$mainUrl/quoc-gia/viet-nam?page=" to "Phim Việt Nam",
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}/tim-kiem?keyword=$query")
            .parsedSafe<MoviesResponse>()
            ?.data?.items
            ?.filterNotNull()
            ?.map {
                it.toSearchResponse()
            } ?: listOf()
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = try {
            app.get(request.data + page).parsed<MoviesResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (subUrl.isEmpty()) {
            subUrl = response?.data?.aPPDOMAINFRONTEND ?: ""
        }
        if (imgUrl.isEmpty()) {
            imgUrl = "${response?.data?.aPPDOMAINCDNIMAGE}/uploads/movies"
        }
        val homeItems = response?.data?.items
            ?.filterNotNull()
            ?.map {
                it.toSearchResponse()
            } ?: listOf()
        return newHomePageResponse(request, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val response = try {
            app.get(url).parsedSafe<MovieDetailResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (response == null) return super.load(url)
        val tvType = if (response.data?.item?.type == "single") {
            TvType.Movie
        } else {
            TvType.TvSeries
        }
        val title = response.data?.item?.name ?: ""
        val actors = response.data?.item?.actor?.filterNotNull() ?: listOf()
        val trailer = response.data?.item?.trailerUrl ?: ""
        val description = response.data?.item?.content ?: ""
        val posterUrl = "$imgUrl/${response.data?.item?.posterUrl}"
        val thumbUrl = "$imgUrl/${response.data?.item?.thumbUrl}"
        val duration = response.data?.item?.time.toInteger()
        val tags = response.data?.item?.category
            ?.filterNotNull()
            ?.map {
                it.name ?: ""
            }
        return if (tvType == TvType.TvSeries) {
            val episodes = response.data?.item?.episodes
                ?.filterNotNull()
                ?.find {
                    it.serverData?.any { data ->
                        !data?.linkM3u8.isNullOrEmpty()
                    } == true
                }
                ?.serverData
                ?.filterNotNull()
                ?.map {
                    Episode(
                        data = it.linkM3u8 ?: "",
                        name = it.filename,
                        posterUrl = thumbUrl
                    )
                } ?: listOf()
            newTvSeriesLoadResponse(
                title,
                url,
                TvType.TvSeries,
                episodes
            ) {
                this.posterUrl = posterUrl
                this.year = response.data?.item?.year
                this.plot = description
                this.duration = duration
                this.showStatus = if (response.data?.item?.status == "completed") {
                    ShowStatus.Completed
                } else {
                    ShowStatus.Ongoing
                }
                this.tags = tags
                addTrailer(trailer)
                addActors(actors)
            }
        } else {
            val link = response.data?.item?.episodes
                ?.firstOrNull()?.serverData
                ?.firstOrNull()?.linkM3u8 ?: ""
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = posterUrl
                this.year = response.data?.item?.year
                this.plot = description
                this.duration = duration
                this.tags = tags
                addTrailer(trailer)
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
        safeApiCall {
            callback.invoke(
                ExtractorLink(
                    "M3U8",
                    "M3U8",
                    data,
                    referer = mainUrl,
                    quality = Qualities.P1080.value,
                    isM3u8 = true
                )
            )
        }
        return true
    }

    private fun MoviesResponse.Data.Item.toSearchResponse(): LiveSearchResponse {
        return LiveSearchResponse(
            name = name ?: "",
            url = "$mainUrl/phim/$slug",
            posterUrl = "${imgUrl}/$thumbUrl",
            type = if (type == "single") TvType.Movie else TvType.TvSeries,
            apiName = "Ổ Phim"
        )
    }
}