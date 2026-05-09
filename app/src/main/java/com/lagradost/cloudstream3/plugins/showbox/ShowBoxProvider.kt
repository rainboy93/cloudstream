package com.lagradost.cloudstream3.plugins.showbox

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
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink

class ShowBoxProvider(val plugin: ShowBoxPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "ShowBox"
    override var mainUrl = "https://showbox.run"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        Pair("hot", "Phim Hot/horizontal"),
        Pair("han-quoc", "Phim Hàn Quốc/vertical"),
        Pair("us-uk", "Phim US-UK/vertical"),
        Pair("trung-quoc", "Phim Trung Quốc/vertical"),
    )

    override val hasMainPage = true
    override val hasDownloadSupport = true

    companion object {
        private const val apiUrl = "https://showbox.run/baseapi/api/v1"
        private const val embedUrl = "https://rophim-api.clubc.org"
        private const val xorKey = "mySecretKey2024"
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val text = app.get("${apiUrl}/movies/search?keyword=${query}").text
        val movies = tryParseJson<SearchResultResponse>(text) ?: return emptyList()
        return movies.result.map { it.toSearchResponse() }
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val name = request.name.split("/")[0]
        val horizontal = request.name.split("/")[1] == "horizontal"

        val movies = if (request.data == "hot") {
            val text = app.get("${apiUrl}/movies/hot").text
            val response = tryParseJson<SearchResultResponse>(text)
            response?.result ?: emptyList()
        } else {
            if (page > 1) return newHomePageResponse(
                list = HomePageList(name, emptyList(), isHorizontalImages = horizontal),
                hasNext = false
            )
            val text = app.get("${apiUrl}/lists/homepageLists?page=1&limit=3").text
            val response = tryParseJson<HomeListsResponse>(text)
            val slug = when (request.data) {
                "han-quoc" -> "phim-han-quoc-moi"
                "us-uk" -> "phim-us-uk-moi"
                "trung-quoc" -> "phim-trung-quoc-moi"
                else -> ""
            }
            response?.result?.collections
                ?.find { it.slug == slug }
                ?.movies ?: emptyList()
        }

        val homePageList = HomePageList(
            name = name,
            list = movies.map { it.toSearchResponse(horizontal) },
            isHorizontalImages = horizontal
        )

        return newHomePageResponse(list = homePageList, hasNext = request.data == "hot")
    }

    private fun parseMovieFromHtml(html: String): MovieItem? {
        val unescaped = html.replace("\\\"", "\"")
        val match = Regex(""""movie":\{"id":\d+""").find(unescaped) ?: return null
        val start = match.range.first + 8
        var depth = 0
        for (i in start until unescaped.length) {
            when (unescaped[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return tryParseJson<MovieItem>(unescaped.substring(start, i + 1))
                    }
                }
            }
        }
        return null
    }

    override suspend fun load(url: String): LoadResponse {
        try {
            val slugWithId = url.substringAfterLast("/")
            val slug = slugWithId.substringBeforeLast(".")
            val movieId = slugWithId.substringAfterLast(".")

            val html = app.get("${mainUrl}/phim/${slug}").text
            val movie = parseMovieFromHtml(html)

            val id = movie?.id?.toString() ?: movieId
            val epsText = app.get("${apiUrl}/episodes/by-idMovie/${id}").text
            val episodes = tryParseJson<List<EpisodeItem>>(epsText) ?: emptyList()

            val movieName = movie?.name ?: slug.replace("-", " ")
            val isSingle = movie?.type == "movie" || episodes.size <= 1

            if (isSingle) {
                val dataUrl = if (episodes.isNotEmpty()) {
                    "${episodes[0].id}@@@${id}"
                } else ""

                return newMovieLoadResponse(movieName, url, TvType.Movie, dataUrl) {
                    this.plot = movie?.description?.replace(Regex("<[^>]*>"), "")
                    this.year = movie?.publishYear
                    addPoster(movie?.poster)
                    addTrailer(movie?.trailerUrl)
                }
            }

            val grouped = episodes.groupBy { it.name }
            val tvEpisodes = grouped.map { (epName, eps) ->
                val dataUrl = eps.joinToString(",") { it.id.toString() } + "@@@${id}"
                newEpisode(
                    url = dataUrl,
                    initializer = {
                        name = "Tập $epName - ${eps.first().server}"
                        posterUrl = eps.first().poster
                    },
                    fix = false
                )
            }

            return newTvSeriesLoadResponse(movieName, url, TvType.TvSeries, tvEpisodes) {
                this.plot = movie?.description?.replace(Regex("<[^>]*>"), "")
                this.year = movie?.publishYear
                addPoster(movie?.poster)
                addTrailer(movie?.trailerUrl)
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
        val parts = data.split("@@@")
        val episodeIds = parts[0].split(",")

        for (episodeId in episodeIds) {
            try {
                val html = app.get(
                    "${embedUrl}/embed/${episodeId}",
                    referer = mainUrl
                ).text

                val episodeMatch = Regex("var episode = (\\{.*?\\});").find(html)
                    ?: continue
                val episode = tryParseJson<EmbedEpisode>(episodeMatch.groupValues[1])
                    ?: continue

                val videoUrl = if (episode.type == "m3u8" && episode.encryptedUrl.isNotEmpty()) {
                    hexXorDecrypt(episode.encryptedUrl, xorKey)
                } else {
                    continue
                }

                callback.invoke(
                    newExtractorLink(
                        source = episode.server,
                        url = videoUrl,
                        name = episode.server,
                        initializer = {
                            referer = mainUrl
                            quality = Qualities.Unknown.value
                            type = ExtractorLinkType.M3U8
                        }
                    )
                )

                val dataMx = Regex("data-mx='([^']*)'").find(html)?.groupValues?.get(1)
                if (dataMx != null) {
                    try {
                        val decoded = String(
                            android.util.Base64.decode(
                                dataMx.reversed(),
                                android.util.Base64.DEFAULT
                            )
                        )
                        val mxData = tryParseJson<MxData>(decoded)
                        mxData?.subtitles?.forEach { sub ->
                            subtitleCallback.invoke(
                                newSubtitleFile(sub.name, sub.vttUrl)
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
        }

        return true
    }

    private fun hexXorDecrypt(hex: String, key: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < hex.length) {
            val byte = hex.substring(i, i + 2).toInt(16)
            val keyByte = key[(i / 2) % key.length].code
            sb.append((byte xor keyByte).toChar())
            i += 2
        }
        return sb.toString()
    }

    private fun MovieItem.toSearchResponse(horizontal: Boolean = false): SearchResponse {
        val movieUrl = "${mainUrl}/phim/${slug}.${id}"
        return newMovieSearchResponse(name, movieUrl, TvType.Movie, true) {
            this.posterUrl = if (horizontal) poster else thumbnail
        }
    }

    data class SearchResultResponse(
        @JsonProperty("status") val status: Boolean,
        @JsonProperty("result") val result: List<MovieItem>,
    )

    data class HomeListsResponse(
        @JsonProperty("status") val status: Boolean,
        @JsonProperty("result") val result: HomeListsResult,
    )

    data class HomeListsResult(
        @JsonProperty("collections") val collections: List<Collection>,
    )

    data class Collection(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("movies") val movies: List<MovieItem>,
    )

    data class MovieItem(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String,
        @JsonProperty("origin_name") val originName: String?,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("description") val description: String?,
        @JsonProperty("thumbnail") val thumbnail: String?,
        @JsonProperty("poster") val poster: String?,
        @JsonProperty("type") val type: String,
        @JsonProperty("trailer_url") val trailerUrl: String?,
        @JsonProperty("publish_year") val publishYear: Int?,
        @JsonProperty("quality") val quality: String?,
        @JsonProperty("categories") val categories: List<Category>?,
    )

    data class Category(
        @JsonProperty("name") val name: String,
        @JsonProperty("slug") val slug: String,
    )

    data class EpisodeItem(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String,
        @JsonProperty("server") val server: String,
        @JsonProperty("slug") val slug: String,
        @JsonProperty("season_number") val seasonNumber: Int,
        @JsonProperty("poster") val poster: String?,
    )

    data class EmbedEpisode(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String,
        @JsonProperty("server") val server: String,
        @JsonProperty("type") val type: String,
        @JsonProperty("encrypted_url") val encryptedUrl: String,
    )

    data class MxData(
        @JsonProperty("subtitles") val subtitles: List<Subtitle>?,
    )

    data class Subtitle(
        @JsonProperty("vtt_url") val vttUrl: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("code") val code: String,
    )
}
