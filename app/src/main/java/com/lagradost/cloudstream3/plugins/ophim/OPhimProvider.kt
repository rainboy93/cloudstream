package com.lagradost.cloudstream3.plugins.ophim

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addPoster
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.nicehttp.NiceResponse

class OPhimProvider(val plugin: OPhimPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "Ổ Phim"
    override var mainUrl = "https://ophim1.com"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        Pair("${mainUrl}/v1/api/danh-sach/phim-moi-cap-nhat", "Phim Mới/vertical"),
        Pair("${mainUrl}/v1/api/danh-sach/phim-le", "Phim Lẻ/horizontal"),
        Pair("${mainUrl}/v1/api/quoc-gia/han-quoc", "Phim Hàn Quốc/vertical"),
        Pair("${mainUrl}/v1/api/danh-sach/hoat-hinh", "Phim Hoạt Hình/vertical"),
        Pair("${mainUrl}/v1/api/quoc-gia/au-my", "Phim Âu - Mỹ/vertical"),
        Pair("${mainUrl}/v1/api/quoc-gia/trung-quoc", "Phim Trung Quốc/vertical"),
        Pair("${mainUrl}/v1/api/danh-sach/phim-chieu-rap", "Phim Chiếu rạp/vertical"),
    )

    override val hasMainPage = true
    override val hasDownloadSupport = true

    var mainUrlImage = "https://ophim17.cc/_next/image?url=https://img.ophim.live/uploads/movies"

    private suspend fun request(url: String): NiceResponse {
        return app.get(url)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return this.getMoviesList("${mainUrl}/v1/api/tim-kiem?keyword=${query}", 1)!!
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val name = request.name.split("/")[0]
        val horizontal = request.name.split("/")[1] == "horizontal"

        val homePageList = HomePageList(
            name = name,
            list = this.getMoviesList(request.data, page, horizontal)!!,
            isHorizontalImages = horizontal
        )

        return newHomePageResponse(list = homePageList, hasNext = true)
    }

    override suspend fun load(url: String): LoadResponse {
        val el = this

        try {
            val text = request(url).text
            val response = tryParseJson<MovieResponse>(text)!!

            val movie = response.movie
            val movieEpisodes = this.mapEpisodesResponse(response.episodes)

            var type = movie.type
            if (type != "single" && type != "series") {
                type = if (movieEpisodes.size > 1) "series" else "single"
            }

            if (type == "single") {
                var dataUrl = "${url}@@@"
                if (movieEpisodes.isNotEmpty()) {
                    dataUrl = "${url}@@@${movieEpisodes[0].slug}"
                }

                return newMovieLoadResponse(movie.name, url, TvType.Movie, dataUrl) {
                    this.plot = movie.content
                    this.year = movie.publishYear
                    this.tags = movie.categories.map { it.name }
                    this.recommendations =
                        el.getMoviesList("${mainUrl}/v1/api/danh-sach/phim-le", 1)
                    addPoster(el.getImageUrl(movie.posterUrl, 1080))
                    addActors(movie.casts.map { cast -> Actor(cast, "") })
                    addTrailer(movie.trailerUrl)
                }
            }

            if (type == "series") {
                val episodes = movieEpisodes.mapNotNull { episode ->
                    val dataUrl = "${url}@@@${episode.slug}"
                    newEpisode(
                        url = dataUrl,
                        initializer = {
                            name = episode.name
                            posterUrl = el.getImageUrl(movie.posterUrl)
                            description = episode.filename
                        }
                    )
                }

                return newTvSeriesLoadResponse(movie.name, url, TvType.TvSeries, episodes) {
                    this.plot = movie.content
                    this.year = movie.publishYear
                    this.tags = movie.categories.map { it.name }
                    this.recommendations =
                        el.getMoviesList("${mainUrl}/v1/api/danh-sach/phim-bo", 1)
                    addPoster(el.getImageUrl(movie.posterUrl))
                    addActors(movie.casts.map { cast -> Actor(cast, "") })
                    addTrailer(movie.trailerUrl)
                }
            }
        } catch (error: Exception) {
        }

        val codeText = "(CODE: ${url.split("/").lastOrNull()})"
        return newMovieLoadResponse("Something went wrong!", url, TvType.Movie, "") {
            this.plot = "There's a problem loading this content. $codeText"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val url = data.split("@@@")[0]
        val slug = data.split("@@@")[1]

        val text = request(url).text
        val response = tryParseJson<MovieResponse>(text)!!

        val episodes = this.mapEpisodesResponse(response.episodes)
        val episodeItem = episodes.find { episode -> episode.slug == slug }

        if (episodeItem !== null) {
            episodeItem.episodes.forEach { episode ->
                callback.invoke(
                    newExtractorLink(
                        source = episode.server,
                        url = episode.linkM3u8.ifEmpty { episode.linkEmbed },
                        name = episode.server,
                        initializer = {
                            referer = mainUrl
                            quality = Qualities.Unknown.value
                            type = if (episode.linkM3u8.isEmpty()) {
                                ExtractorLinkType.VIDEO
                            } else {
                                ExtractorLinkType.M3U8
                            }
                        }
                    )
                )
            }
        }

        return true
    }

    data class ListResponse(
        @JsonProperty("data") val data: ListDataResponse,
    )

    data class ListDataResponse(
        @JsonProperty("items") val items: List<MoviesResponse>,
    )

    data class MoviesResponse(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("thumb_url") val thumbUrl: String,
        @JsonProperty("poster_url") val posterUrl: String,
    )

    data class MovieResponse(
        @JsonProperty("movie") val movie: MovieDetailResponse,
        @JsonProperty("episodes") val episodes: List<MovieEpisodeResponse>,
    )

    data class MovieDetailResponse(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("type") val type: String,
        @JsonProperty("content") val content: String,
        @JsonProperty("thumb_url") val thumbUrl: String,
        @JsonProperty("poster_url") val posterUrl: String,
        @JsonProperty("trailer_url") val trailerUrl: String,
        @JsonProperty("year") val publishYear: Int,
        @JsonProperty("actor") val casts: List<String>,
        @JsonProperty("category") val categories: List<MovieTaxonomyResponse>,
    )

    data class MovieTaxonomyResponse(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
    )

    data class MovieEpisodeResponse(
        @JsonProperty("server_name") val serverName: String,
        @JsonProperty("server_data") val serverData: List<MovieEpisodeDataResponse>,
    )

    data class MovieEpisodeDataResponse(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("filename") val filename: String,
        @JsonProperty("link_m3u8") val linkM3u8: String,
        @JsonProperty("link_embed") val linkEmbed: String,
    )

    data class MappedData(
        val name: String,
        val slug: String,
        val filename: String,
        val server: String,
        val linkM3u8: String,
        val linkEmbed: String,
    )

    data class MappedEpisode(
        val name: String,
        val slug: String,
        val filename: String,
        val episodes: MutableList<MappedEpisodeItem> = mutableListOf(),
    )

    data class MappedEpisodeItem(
        val server: String,
        val linkM3u8: String,
        val linkEmbed: String,
    )

    private fun getImageUrl(url: String, width: Int = 256): String {
        var newUrl = url
        if (!url.contains("http")) {
            newUrl = if (url.first() == '/')
                "${mainUrlImage}${url}&w=$width&q=75" else "${mainUrlImage}/${url}&w=$width&q=75"
        }
        return newUrl
    }

    private suspend fun getMoviesList(
        url: String,
        page: Int,
        horizontal: Boolean = false,
    ): List<SearchResponse>? {
        val el = this

        try {
            var newUrl = "${url}?page=${page}"
            if (url.contains("?")) {
                newUrl = "${url}&page=${page}"
            }

            val text = request(newUrl).text
            val response = tryParseJson<ListResponse>(text)

            return response?.data?.items?.map { movie ->
                val movieUrl = "${mainUrl}/phim/${movie.slug}"
                newMovieSearchResponse(movie.name, movieUrl, TvType.Movie, true) {
                    this.posterUrl =
                        if (horizontal) el.getImageUrl(movie.posterUrl) else el.getImageUrl(movie.thumbUrl)
                }
            }
        } catch (error: Exception) {
        }

        return mutableListOf<SearchResponse>()
    }

    private suspend fun mapEpisodesResponse(episodes: List<MovieEpisodeResponse>): List<MappedEpisode> {
        return episodes
            .flatMap { episode ->
                episode.serverData.map { item ->
                    MappedData(
                        name = item.name,
                        slug = item.slug,
                        filename = item.filename,
                        server = episode.serverName,
                        linkM3u8 = item.linkM3u8,
                        linkEmbed = item.linkEmbed
                    )
                }.filter { data ->
                    data.name.isNotEmpty()
                }
            }
            .fold(mutableMapOf<String, MappedEpisode>()) { accumulator, current ->
                val key = current.name
                val episode = accumulator.getOrPut(key) {
                    MappedEpisode(
                        name = current.name,
                        slug = current.slug,
                        filename = current.filename,
                    )
                }
                episode.episodes.add(
                    MappedEpisodeItem(
                        server = current.server,
                        linkM3u8 = current.linkM3u8,
                        linkEmbed = current.linkEmbed
                    )
                )
                accumulator
            }
            .values
            .sortedBy { it.name }
    }
}