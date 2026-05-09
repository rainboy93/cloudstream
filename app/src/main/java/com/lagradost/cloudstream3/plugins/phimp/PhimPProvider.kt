package com.lagradost.cloudstream3.plugins.phimp

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
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

class PhimPProvider(val plugin: PhimPPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "PhimP"
    override var mainUrl = "https://phimp.me"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        Pair("${apiUrl}/movies/list?", "Phim Mới/vertical"),
        Pair("${apiUrl}/movies/list?type=1&", "Phim Lẻ/horizontal"),
        Pair("${apiUrl}/movies/list?type=2&", "Phim Bộ/vertical"),
        Pair("${apiUrl}/movies/list?countries=han-quoc&", "Phim Hàn Quốc/vertical"),
    )

    override val hasMainPage = true
    override val hasDownloadSupport = true

    companion object {
        private const val apiUrl = "https://admin.phimp.me/wp-json/app/v1"
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return getMoviesList("${apiUrl}/movies/list?q=${query}&", 1) ?: emptyList()
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val name = request.name.split("/")[0]
        val horizontal = request.name.split("/")[1] == "horizontal"

        val homePageList = HomePageList(
            name = name,
            list = getMoviesList(request.data, page, horizontal) ?: emptyList(),
            isHorizontalImages = horizontal
        )

        return newHomePageResponse(list = homePageList, hasNext = true)
    }

    override suspend fun load(url: String): LoadResponse {
        try {
            val publicId = url.substringAfterLast(".")
            val text = app.get("${apiUrl}/movies/detail/${publicId}").text
            val response = tryParseJson<DetailResponse>(text) ?: throw Exception("Parse error")
            val movie = response.result

            val episodes = movie.seasons.flatMap { season ->
                season.serverData.map { ep ->
                    MappedData(
                        name = ep.name,
                        slug = ep.slug,
                        filename = ep.filename,
                        server = season.serverName,
                        linkM3u8 = ep.linkM3u8,
                        linkEmbed = ep.linkEmbed,
                    )
                }
            }.filter { it.name.isNotEmpty() }

            val mappedEpisodes =
                episodes.fold(mutableMapOf<String, MappedEpisode>()) { acc, current ->
                    val episode = acc.getOrPut(current.name) {
                        MappedEpisode(
                            name = current.name,
                            slug = current.slug,
                            filename = current.filename
                        )
                    }
                    episode.episodes.add(
                        MappedEpisodeItem(
                            current.server,
                            current.linkM3u8,
                            current.linkEmbed
                        )
                    )
                    acc
                }.values.sortedBy { it.name }

            val isSingle = mappedEpisodes.size <= 1

            if (isSingle) {
                var dataUrl = "${publicId}@@@"
                if (mappedEpisodes.isNotEmpty()) {
                    dataUrl = "${publicId}@@@${mappedEpisodes[0].slug}"
                }

                return newMovieLoadResponse(movie.name, url, TvType.Movie, dataUrl) {
                    this.plot = movie.overview
                    this.year = movie.year
                    addPoster(movie.poster)
                    addTrailer(movie.trailerUrl)
                }
            }

            val tvEpisodes = mappedEpisodes.mapNotNull { episode ->
                val dataUrl = "${publicId}@@@${episode.slug}"
                newEpisode(
                    url = dataUrl,
                    initializer = {
                        name = episode.name
                        posterUrl = movie.poster
                        description = episode.filename
                    },
                    fix = false
                )
            }

            return newTvSeriesLoadResponse(movie.name, url, TvType.TvSeries, tvEpisodes) {
                this.plot = movie.overview
                this.year = movie.year
                addPoster(movie.poster)
                addTrailer(movie.trailerUrl)
            }
        } catch (_: Exception) {
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
        val publicId = data.split("@@@")[0]
        val slug = data.split("@@@")[1]

        val text = app.get("${apiUrl}/movies/detail/${publicId}").text
        val response = tryParseJson<DetailResponse>(text) ?: return false

        val allEpisodes = response.result.seasons.flatMap { season ->
            season.serverData.map { ep ->
                MappedData(
                    ep.name,
                    ep.slug,
                    ep.filename,
                    season.serverName,
                    ep.linkM3u8,
                    ep.linkEmbed
                )
            }
        }

        val matched = allEpisodes.filter { it.slug == slug }
        matched.forEach { episode ->
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

        return true
    }

    private suspend fun getMoviesList(
        url: String,
        page: Int,
        horizontal: Boolean = false,
    ): List<SearchResponse>? {
        try {
            val text = app.get("${url}page=${page}").text
            val response = tryParseJson<ListResponse>(text) ?: return null

            return response.result.items.map { movie ->
                val movieUrl = "${mainUrl}/phim/${movie.slug}.${movie.publicId}"
                newMovieSearchResponse(movie.name, movieUrl, TvType.Movie, true) {
                    this.posterUrl = if (horizontal) movie.poster else movie.thumb
                }
            }
        } catch (_: Exception) {
        }

        return emptyList()
    }

    data class ListResponse(
        @JsonProperty("status") val status: Boolean,
        @JsonProperty("result") val result: ListResult,
    )

    data class ListResult(
        @JsonProperty("items") val items: List<MovieItem>,
        @JsonProperty("page") val page: Int,
        @JsonProperty("page_count") val pageCount: Int,
        @JsonProperty("total") val total: Int,
        @JsonProperty("hasMore") val hasMore: Boolean,
    )

    data class DetailResponse(
        @JsonProperty("status") val status: Boolean,
        @JsonProperty("result") val result: MovieDetail,
    )

    data class MovieItem(
        @JsonProperty("public_id") val publicId: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("poster") val poster: String,
        @JsonProperty("thumb") val thumb: String,
        @JsonProperty("type") val type: Int,
    )

    data class MovieDetail(
        @JsonProperty("public_id") val publicId: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("overview") val overview: String,
        @JsonProperty("poster") val poster: String,
        @JsonProperty("thumb") val thumb: String,
        @JsonProperty("type") val type: Int,
        @JsonProperty("year") val year: Int,
        @JsonProperty("trailer_url") val trailerUrl: String,
        @JsonProperty("seasons") val seasons: List<Season>,
    )

    data class Season(
        @JsonProperty("server_key") val serverKey: Int,
        @JsonProperty("server_name") val serverName: String,
        @JsonProperty("server_data") val serverData: List<EpisodeData>,
    )

    data class EpisodeData(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("filename") val filename: String,
        @JsonProperty("link_embed") val linkEmbed: String,
        @JsonProperty("link_m3u8") val linkM3u8: String,
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
}
