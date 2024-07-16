package com.lagradost.cloudstream3.plugins.bluphim

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.apmap
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

class Phim1080Provider(val plugin: Phim1080Plugin) : MainAPI() {
    override var mainUrl = "https://phim1080.in"
    override var name = "Phim1080"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/phim-le?page=" to "Phim Lẻ",
        "$mainUrl/phim-bo?page=" to "Phim Bộ",
        "$mainUrl/phim-han-quoc?page=" to "Phim Hàn Quốc",
        "$mainUrl/phim-trung-quoc?page=" to "Phim Trung Quốc",
        "$mainUrl/phim-my?page=" to "Phim Mỹ"
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}/tim-kiem/$query")
            .document
            .select(Evaluator.Class("tray-item"))
            .mapNotNull {
                it.toSearchResponse()
            }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val homeItems = document.select(Evaluator.Class("tray-item"))
            .mapNotNull {
                it.toSearchResponse()
            }
        return newHomePageResponse(request.name, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val fId = document.selectFirst(Evaluator.Class("container"))?.attr("data-id")
        val filmInfo = app.get(
            "$mainUrl/api/v2/films/$fId",
            referer = url,
            headers = mapOf(
                "Content-Type" to "application/json",
                "X-Requested-With" to "XMLHttpRequest"
            )
        ).parsedSafe<FilmInfo>()

        val info = document.selectFirst(Evaluator.Class("film-info")) ?: return null
        val posterUrl = filmInfo?.thumbnail
        val poster = filmInfo?.thumbnail
        val background = filmInfo?.poster
        val title = info.selectFirst(Evaluator.Class("film-info-title"))?.text() ?: ""
        val slug = filmInfo?.slug
        val link = "$mainUrl/$slug"
        val description = info.selectFirst(Evaluator.Class("film-info-description"))
            ?.selectFirst(Evaluator.Tag("p"))?.text() ?: ""
        val tvType = if (document.select(Evaluator.ContainsText("TV Series")).isNotEmpty()) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
        val tags =
            info.selectFirst(Evaluator.Class("film-info-tag"))?.allElements?.map { it.text() }
        val year = filmInfo?.year
        val trailerCode = filmInfo?.trailer?.original?.id
        val trailer = "https://www.youtube.com/embed/$trailerCode"
        val recommendations = document.select(Evaluator.Class("related-item"))
            .map {
                val relatedLink = it.selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
                val name = it.selectFirst(Evaluator.Class("related-item-title"))?.text() ?: ""
                val img = it.selectFirst("img")?.attr("data-src") ?: ""
                LiveSearchResponse(
                    name = name,
                    url = fixUrl(relatedLink),
                    posterUrl = fixUrl(img),
                    type = TvType.Movie,
                    apiName = "Phim1080"
                )
            }
        return if (tvType == TvType.TvSeries) {
            val epsInfo = app.get(
                "$mainUrl/api/v2/films/$fId/episodes?sort=name",
                referer = link,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "X-Requested-With" to "XMLHttpRequest",
                )
            ).parsedSafe<MediaDetailEpisodes>()?.eps?.map { ep ->
                Episode(
                    data = fixUrl(ep.link.toString()),
                    name = ep.detailname,
                    episode = ep.episodeNumber,
                )
            } ?: listOf()
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, epsInfo) {
                this.posterUrl = poster
                this.backgroundPosterUrl = background
                this.year = year
                this.plot = description
                this.tags = tags
                addTrailer(trailer)
                this.recommendations = recommendations
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = poster
                this.backgroundPosterUrl = background
                this.year = year
                this.plot = description
                this.tags = tags
                addTrailer(trailer)
                this.recommendations = recommendations
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
        val fId = document.select("div.container").attr("data-id")
        val epId = document.select("div.container").attr("data-episode-id")
        val doc = app.get(
            "$mainUrl/api/v2/films/$fId/episodes/$epId",
            referer = data,
            headers = mapOf(
                "Content-Type" to "application/json",
                "cookie" to "phimnhanh=%3D",
                "X-Requested-With" to "XMLHttpRequest"
            )
        )

        val optEncode = if (doc.text.indexOf("\"opt\":\"") != -1) {
            doc.text.substringAfter("\"opt\":\"").substringBefore("\"}")
        } else {
            ""
        }
        val opt = decodeString(optEncode as String, 69).replace("0uut$", "_")
            .replace("index.m3u8", "3000k/hls/mixed.m3u8")
        val hlsEncode = if (doc.text.indexOf(":{\"hls\":\"") != -1) {
            doc.text.substringAfter(":{\"hls\":\"").substringBefore("\"},")
        } else {
            ""
        }
        val hls = decodeString(hlsEncode as String, 69)
        val fb = if (doc.text.indexOf("\"fb\":[{\"src\":\"") != -1) {
            doc.text.substringAfter("\"fb\":[{\"src\":\"").substringBefore("\",").replace("\\", "")
        } else {
            ""
        }

        listOfNotNull(
            if (hls.contains(".m3u8")) {
                Triple(hls, "HS", true)
            } else null,
            if (fb.contains(".mp4")) {
                Triple(fb, "FB", false)
            } else null,
            if (opt.contains(".m3u8")) {
                Triple(opt, "OP", true)
            } else null,
        ).apmap { (link, source, isM3u8) ->
            safeApiCall {
                callback.invoke(
                    ExtractorLink(
                        source,
                        source,
                        link,
                        referer = data,
                        quality = Qualities.Unknown.value,
                        isM3u8,
                    )
                )
            }
        }
        val subId = doc.parsedSafe<Media>()?.subtitle?.vi
        val isSubIdEmpty = subId.isNullOrBlank()
        if (!isSubIdEmpty) {
            subtitleCallback.invoke(
                SubtitleFile(
                    "Vietnamese",
                    "$mainUrl/subtitle/$subId.vtt"
                )
            )
        }
        return true
    }

    private fun decodeString(e: String, t: Int): String {
        var a = ""
        for (element in e) {
            val r = element.code
            val o = r xor t
            a += o.toChar()
        }
        return a
    }

    private fun Element.toSearchResponse(): LiveSearchResponse {
        val link = selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
        val name = selectFirst(Evaluator.Class("tray-item-title"))?.text() ?: ""
        val img = selectFirst("img")?.attr("data-src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = fixUrl(link),
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "Phim1080"
        )
    }

    data class FilmInfo(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("poster") val poster: String? = null,
        @JsonProperty("thumbnail") val thumbnail: String? = null,
        @JsonProperty("slug") val slug: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("trailer") val trailer: TrailerInfo? = null,
    )

    data class TrailerInfo(
        @JsonProperty("original") val original: TrailerKey? = null,
    )

    data class TrailerKey(
        @JsonProperty("id") val id: String? = null,
    )

    data class MediaDetailEpisodes(
        @JsonProperty("data") val eps: ArrayList<Episodes>? = arrayListOf(),
    )

    data class Episodes(
        @JsonProperty("link") val link: String? = null,
        @JsonProperty("detail_name") val detailname: String? = null,
        @JsonProperty("name") val episodeNumber: Int? = null,
    )

    data class Media(
        @JsonProperty("subtitle") val subtitle: SubInfo? = null,
    )

    data class SubInfo(
        @JsonProperty("vi") val vi: String? = null,
    )
}