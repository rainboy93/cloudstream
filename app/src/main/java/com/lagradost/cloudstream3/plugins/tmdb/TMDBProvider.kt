package com.lagradost.cloudstream3.plugins.tmdb

import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.MovieLoadResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvSeriesLoadResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPage
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.toRatingInt
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

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
                response?.seasons?.map { it.toSearchResponse(response) } ?: listOf())
        } else {
            val isMovie = request.name == "Movies"
            val response =
                app.get((request.data + page).appendApiKey()).parsedSafe<MoviesResponse>()
            return newHomePageResponse(
                request,
                response?.movies?.map { it.toSearchResponse(isMovie) } ?: listOf())
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val isMovie = !url.contains("tv")
        return if (isMovie) {
            loadMoviesDetail(url)
        } else {
            loadTVSeasonsDetail(url)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val dataSplit = data.split("|")
        val dataMap = mutableMapOf<String, String>()
        dataMap["id"] = dataSplit[1]
        dataMap["type"] = dataSplit.first()
        if (dataSplit.first() == "tv") {
            dataMap["season"] = dataSplit[2]
            dataMap["episode"] = dataSplit[3]
        }
        val response = app.post(
            "https://filemoon-extractor.vercel.app/api/stream/f2cloud",
            data = dataMap
        ).parsedSafe<SourceResponse>()
        response?.subtitles?.forEach {
            subtitleCallback.invoke(
                SubtitleFile(it.label ?: "", it.url ?: "")
            )
        }
        safeApiCall {
            callback.invoke(
                ExtractorLink(
                    "TMDB",
                    "TMDB",
                    response?.source ?: "",
                    referer = "",
                    quality = Qualities.P1080.value,
                    isM3u8 = true,
                )
            )
        }
        return true
    }

    private suspend fun loadMoviesDetail(url: String): MovieLoadResponse {
        val response = app.get((url).appendApiKey()).parsedSafe<MovieDetailResponse>()
        return newMovieLoadResponse(
            response?.title ?: "",
            url,
            TvType.Movie,
            "movie|${response?.id}"
        ) {
            this.posterUrl = response?.backdropPath.imageUrl()
            this.year = response?.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull()
            this.plot = response?.overview
            this.rating = response?.voteAverage.toRatingInt()
        }
    }

    private suspend fun loadTVSeasonsDetail(url: String): TvSeriesLoadResponse {
        val id = url.substringAfter("tv/").substringBefore("/season")
        val seasonNumber = url.substringAfter("season/").substringBefore("?")
        val response = app.get((url).appendApiKey()).parsedSafe<TVSeasonsDetailResponse>()
        val episodes = response?.episodes?.map {
            Episode(
                data = "tv|$id|$seasonNumber|${it.seasonNumber}",
                name = it.name,
                episode = it.episodeNumber,
                posterUrl = it.stillPath.imageUrl()
            )
        } ?: listOf()
        return newTvSeriesLoadResponse(response?.name ?: "", url, TvType.TvSeries, episodes) {
            this.posterUrl = response?.posterPath.imageUrl()
            this.year = response?.airDate?.split("-")?.firstOrNull()?.toIntOrNull()
            this.plot = ""
            this.rating = response?.voteAverage.toRatingInt()
        }
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

    private fun TVSeriesDetailResponse.Season.toSearchResponse(response: TVSeriesDetailResponse?): LiveSearchResponse {
        val url = fixUrl("tv/${response?.id}/season/${seasonNumber}".appendApiKey())
        return LiveSearchResponse(
            name = "${response?.name} ($name)",
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