package com.lagradost.cloudstream3.plugins.bluphim

import android.net.Uri
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
import org.json.JSONArray
import org.json.JSONObject
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

        val poster = document.selectFirst(Evaluator.Class("poster")) ?: return null
        val img = poster.selectFirst(Evaluator.Class("adspruce-streamlink"))
            ?.selectFirst(Evaluator.Tag("img"))
        val posterUrl = img?.attr("src") ?: ""
        val title = img?.attr("title") ?: ""
        val youtubeTrailer =
            poster.selectFirst(Evaluator.Class("btn-see btn btn-primary btn-download-link"))
                ?.attr("href") ?: ""
        val link = poster.selectFirst(Evaluator.Class("btn-see btn btn-danger btn-stream-link"))
            ?.attr("href") ?: ""
        val rating = document.select(Evaluator.Class("film-status")).lastOrNull()
            ?.select("a")?.text()
        val details = document.selectFirst(Evaluator.Class("detail"))
        val description =
            details?.selectFirst(Evaluator.Id("info-film"))?.selectFirst(Evaluator.Class("tab"))
                ?.selectFirst(Evaluator.Tag("p"))?.text()
        val tvType = if (document.select(Evaluator.ContainsText("TV Series")).isNotEmpty()) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
        val actors =
            poster.selectFirst(Evaluator.Class("dienviendd"))?.allElements?.map { it.text() }
        val tags = poster.selectFirst(Evaluator.Class("theloaidd"))?.allElements?.map { it.text() }
        val year = poster.selectFirst(Evaluator.Class("dinfo"))?.run {

        }
        return if (tvType == TvType.TvSeries) {
            val docEpisodes = app.get(fixUrl(link)).document
            val episodes = docEpisodes.select(Evaluator.Class("list-episode")).lastOrNull()
                ?.select(Evaluator.Tag("a"))?.map {
                    val href = it.attr("href")
                    val episode = it.text().replace(Regex("[^0-9]"), "").trim().toIntOrNull()
                    val name = "Episode $episode"
                    Episode(
                        data = href,
                        name = name,
                        episode = episode,
                    )
                } ?: listOf()
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = fixUrl(posterUrl)
//                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating?.toIntOrNull()
                addActors(actors)
                addTrailer(youtubeTrailer)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = fixUrl(posterUrl)
                this.plot = description
                this.tags = tags
                this.rating = rating?.toIntOrNull()
                addActors(actors)
                addTrailer(youtubeTrailer)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        var document = app.get(fixUrl(data)).document
        val ref =
            document.selectFirst(Evaluator.Id("iframeStream"))?.attr("src")
                ?: return super.loadLinks(
                    data,
                    isCasting,
                    subtitleCallback,
                    callback
                )
        val refUri = Uri.parse(ref)
        document = app.get(
            url = fixUrl(ref),
            referer = "https://bluphim.net/"
        ).document
        val videoId = ref.substringAfter("id=").substringBefore("&")
        val script = document.select(Evaluator.Tag("script")).map { it.data() }
            .firstOrNull { it.contains("ShowLoading()") }
        if (!script.isNullOrBlank()) {
            val token = app.post(
                // Not mainUrl
                url = "${refUri.host}/geturl",
                data = mapOf(
                    "videoId" to videoId,
                    "domain" to ref,
                    "renderer" to "Apple",
                    "id" to videoId
                ),
                referer = ref,
                headers = mapOf(
                    "X-Requested-With" to "XMLHttpRequest",
                    "Content-Type" to "multipart/form-data;"
                )
            ).text.also { println("BLU $it") }

            val cdn = script.substringAfter("cdn = '").substringBefore("'")
            val cdnUrl = "$cdn/streaming?id=$videoId&web=BluPhim.Net&$token&cdn=$cdn&lang=vi"
            document = app.get(cdnUrl).document
            val content = document.selectFirst(Evaluator.ContainsText(videoId))?.text()
                ?: return super.loadLinks(data, isCasting, subtitleCallback, callback)
            val videoUrl =
                "${content.substringAfter("url = '").substringBefore("'")}$videoId/?$token"
            try {
                val subtitleTracks = "${content.substringAfter("").substringBefore("}]")}}]"
                val jsonArray = JSONArray(subtitleTracks)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = JSONObject(jsonArray.get(i).toString())
                    if (jsonObject.get("label") == "Vietnamese") {
                        subtitleCallback.invoke(
                            SubtitleFile(
                                "vi",
                                jsonObject.get("file").toString()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            safeApiCall {
                callback.invoke(
                    ExtractorLink(
                        "CDN",
                        "CDN",
                        videoUrl,
                        referer = "$mainUrl/",
                        quality = Qualities.P1080.value,
                        isM3u8 = true
                    )
                )
            }
        } else {
            val iframe = document.selectFirst(Evaluator.Tag("iframe"))
                ?: return true
            val cdnUrl = iframe.attr("src")
            document = app.get(
                url = cdnUrl,
                referer = "${refUri.scheme}://${refUri.host}/"
            ).document
            val videoUrl = document.data().substringAfter("url = '").substringBefore("'")
            safeApiCall {
                callback.invoke(
                    ExtractorLink(
                        "CDN",
                        "CDN",
                        videoUrl,
                        referer = "$mainUrl/",
                        quality = Qualities.P1080.value,
                        isM3u8 = true
                    )
                )
            }
        }
        return true
    }

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val link = selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
        val name = selectFirst(Evaluator.Class("tray-item-title"))?.text() ?: ""
        val img = selectFirst("img")?.attr("src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = fixUrl(link),
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "Phim1080"
        )
    }
}