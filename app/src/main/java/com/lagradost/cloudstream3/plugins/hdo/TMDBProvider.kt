package com.lagradost.cloudstream3.plugins.hdo

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

class TMDBProvider : MainAPI() {

    override var mainUrl = "https://api.themoviedb.org/3"
    override var name = "TMDB"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasDownloadSupport: Boolean = false
    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        mainPage(
            "$mainUrl/discover/movie?sort_by=popularity.desc&with_genres&page=",
            "Movies"
        ),
        mainPage(
            "$mainUrl/discover/tv?sort_by=popularity.desc&with_genres&page=",
            "TV Series"
        ),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (request.data.startsWith("custom")) {
            val response =
                app.get((request.data.replace("custom", "") + page).appendApiKey())
                    .parsedSafe<TVSeriesDetailResponse>()
            return newHomePageResponse(
                request,
                response?.seasons?.map { it.toSearchResponse(response.name ?: "") } ?: listOf())
        } else {
            val isMovie = request.name == "Movies"
            val response =
                app.get((request.data + page).appendApiKey()).parsedSafe<MoviesResponse>()
            return newHomePageResponse(
                request,
                response?.movies?.map { it.toSearchResponse(isMovie) } ?: listOf())
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val isMovie = url.contains("movie")
        val response = app.get((url).appendApiKey()).parsedSafe<MovieDetailResponse>()
        return super.load(url)
    }

    private fun MoviesResponse.Movie.toSearchResponse(isMovie: Boolean): LiveSearchResponse {
        val url = fixUrl("${if (isMovie) "movie" else "tv"}/$id".appendApiKey())
        return LiveSearchResponse(
            name = title ?: "",
            url = url,
            posterUrl = posterPath.imageUrl(),
            type = if (isMovie) TvType.Movie else TvType.TvSeries,
            apiName = "TMDB"
        )
    }

    private fun TVSeriesDetailResponse.Season.toSearchResponse(movieName: String): LiveSearchResponse {
        val url = fixUrl("tv/$id".appendApiKey())
        return LiveSearchResponse(
            name = "$movieName $name",
            url = url,
            posterUrl = posterPath.imageUrl(),
            type = TvType.TvSeries,
            apiName = "TMDB"
        )
    }

    private fun String?.imageUrl(): String {
        return "https://image.tmdb.org/t/p/w500$this"
    }

    private fun String.appendApiKey(): String {
        return if (this.contains("?")) {
            "$this&api_key=$API_KEY&include_adult=false&include_video=false&language=vi-VN"
        } else {
            "$this?api_key=$API_KEY&include_adult=false&include_video=false&language=vi-VN"
        }
    }

    companion object {
        private const val API_KEY = "fb7bb23f03b6994dafc674c074d01761"
    }
}