package com.lagradost.cloudstream3.plugins.phim4k

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
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
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import java.net.URLEncoder

class Phim4kProvider(val plugin: Phim4kPlugin) : MainAPI() {
    override var lang = "vi"
    override var name = "Phim4K"
    override var mainUrl = "https://fr22.phim4k.dpdns.org"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true
    override val hasDownloadSupport = true

    private val apiUrl = "$mainUrl/api"

    override val mainPage = mainPageOf(
        Pair("$apiUrl/films/phim-moi-cap-nhat", "Phim Mới Cập Nhật"),
        Pair("$apiUrl/films/the-loai/hanh-dong", "Hành Động"),
        Pair("$apiUrl/films/the-loai/tinh-cam", "Tình Cảm"),
        Pair("$apiUrl/films/the-loai/co-trang", "Cổ Trang"),
        Pair("$apiUrl/films/the-loai/hoat-hinh", "Hoạt Hình"),
        Pair("$apiUrl/films/quoc-gia/han-quoc", "Phim Hàn Quốc"),
        Pair("$apiUrl/films/quoc-gia/trung-quoc", "Phim Trung Quốc"),
        Pair("$apiUrl/films/quoc-gia/au-my", "Phim Âu - Mỹ"),
        Pair("$apiUrl/films/quoc-gia/viet-nam", "Phim Việt Nam"),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = tryParseJson<Phim4kListResponse>(app.get("${request.data}?page=$page").text)
        val list = response?.items.orEmpty().mapNotNull { it.toSearchResponse() }
        val hasNext = response?.paginate?.let { page < (it.totalPage ?: 0) } ?: list.isNotEmpty()

        return newHomePageResponse(
            list = HomePageList(name = request.name, list = list, isHorizontalImages = false),
            hasNext = hasNext
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val response =
            tryParseJson<Phim4kListResponse>(app.get("$apiUrl/films/search?keyword=$q").text)
        return response?.items.orEmpty().mapNotNull { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse {
        val response = tryParseJson<Phim4kDetailResponse>(app.get(url).text)
        val movie = response?.movie ?: return newMovieLoadResponse(
            "Không tải được nội dung", url, TvType.Movie, "[]"
        ) {}

        val poster = movie.posterUrl ?: movie.thumbUrl
        val title = movie.name ?: movie.originalName ?: "Phim4K"

        // Group each server's playable item by its episode label.
        val grouped = LinkedHashMap<String, MutableList<Phim4kStreamLink>>()
        for (server in movie.episodes.orEmpty()) {
            for (item in server.items.orEmpty()) {
                val link = item.link?.takeIf { it.isNotBlank() } ?: continue
                val key = item.name ?: item.slug ?: continue
                grouped.getOrPut(key) { mutableListOf() }
                    .add(Phim4kStreamLink(server.serverName, link))
            }
        }

        val isSeries = (movie.totalEpisodes ?: 0) > 1 ||
                movie.tmdb?.type.equals("tv", ignoreCase = true) ||
                grouped.size > 1

        if (!isSeries) {
            val data = grouped.values.firstOrNull()?.toJson() ?: "[]"
            return newMovieLoadResponse(title, url, TvType.Movie, data) {
                this.posterUrl = poster
                this.year = movie.resolvedYear()
                this.plot = movie.description
                this.score = movie.tmdb?.voteAverage?.let { Score.from(it, 10) }
                addPoster(poster)
            }
        }

        val episodes = grouped.entries
            .sortedBy { episodeNumberOf(it.key) ?: Int.MAX_VALUE }
            .mapIndexed { index, entry ->
                newEpisode(entry.value.toJson()) {
                    this.name = "Tập ${entry.key}"
                    this.episode = episodeNumberOf(entry.key) ?: (index + 1)
                    this.posterUrl = poster
                }
            }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year = movie.resolvedYear()
            this.plot = movie.description
            this.score = movie.tmdb?.voteAverage?.let { Score.from(it, 10) }
            addPoster(poster)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val links = tryParseJson<List<Phim4kStreamLink>>(data) ?: return false
        var found = false

        links.forEach { stream ->
            val link = stream.link?.takeIf { it.isNotBlank() } ?: return@forEach
            val label = stream.server ?: name
            found = true
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = "$name - $label",
                    url = link,
                ) {
                    referer = "$mainUrl/"
                    quality = Qualities.Unknown.value
                    type = ExtractorLinkType.M3U8
                }
            )
        }

        return found
    }

    /** Extracts the episode's numeric index from values like "1", "Tập 10", "Full". */
    private fun episodeNumberOf(text: String?): Int? =
        text?.let { Regex("\\d+").find(it)?.value?.toIntOrNull() }

    private fun Phim4kMovie.toSearchResponse(): SearchResponse? {
        val slug = this.slug ?: return null
        val title = this.name ?: this.originalName ?: return null
        return newMovieSearchResponse(title, "$apiUrl/film/$slug", TvType.Movie) {
            this.posterUrl = this@toSearchResponse.posterUrl ?: this@toSearchResponse.thumbUrl
        }
    }

    private fun Phim4kMovie.resolvedYear(): Int? {
        year?.let { if (it > 0) return it }
        return created?.takeIf { it.length >= 4 }?.substring(0, 4)?.toIntOrNull()
    }

    @Keep
    data class Phim4kListResponse(
        @JsonProperty("status") val status: String? = null,
        @JsonProperty("items") val items: List<Phim4kMovie>? = null,
        @JsonProperty("paginate") val paginate: Phim4kPaginate? = null,
    )

    @Keep
    data class Phim4kPaginate(
        @JsonProperty("current_page") val currentPage: Int? = null,
        @JsonProperty("total_page") val totalPage: Int? = null,
    )

    @Keep
    data class Phim4kDetailResponse(
        @JsonProperty("status") val status: String? = null,
        @JsonProperty("movie") val movie: Phim4kMovie? = null,
    )

    @Keep
    data class Phim4kMovie(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("original_name") val originalName: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("poster_url") val posterUrl: String? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("quality") val quality: String? = null,
        @JsonProperty("time") val time: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("created") val created: String? = null,
        @JsonProperty("total_episodes") val totalEpisodes: Int? = null,
        @JsonProperty("tmdb") val tmdb: Phim4kTmdb? = null,
        @JsonProperty("episodes") val episodes: List<Phim4kServer>? = null,
    )

    @Keep
    data class Phim4kTmdb(
        @JsonProperty("type") val type: String? = null,
        @JsonProperty("vote_average") val voteAverage: Double? = null,
    )

    @Keep
    data class Phim4kServer(
        @JsonProperty("server_name") val serverName: String? = null,
        @JsonProperty("items") val items: List<Phim4kItem>? = null,
    )

    @Keep
    data class Phim4kItem(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("link") val link: String? = null,
    )

    @Keep
    data class Phim4kStreamLink(
        @JsonProperty("server") val server: String? = null,
        @JsonProperty("link") val link: String? = null,
    )
}
