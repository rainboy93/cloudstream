package com.lagradost.cloudstream3.plugins.bluphim

import android.net.Uri
import com.blankj.utilcode.util.LogUtils
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
import com.lagradost.cloudstream3.mainPage
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.plugins.toInteger
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class BluPhimProvider(val plugin: BluPhimPlugin) : MainAPI() {
    override var mainUrl = "https://bluphim.art"
    override var name = "Blu Phim"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        mainPage("$mainUrl/the-loai/phim-le-", "Phim Lẻ"),
        mainPage("$mainUrl/the-loai/phim-bo-", "Phim Bộ"),
        mainPage("$mainUrl/quoc-gia/han-quoc-", "Phim Hàn Quốc"),
        mainPage("$mainUrl/trung-quoc-hong-kong-", "Phim Trung Quốc"),
        mainPage("$mainUrl/quoc-gia/au-my-", "Phim Âu Mỹ"),
        mainPage("$mainUrl/tuyen-tap-", "Tuyển tập", true),
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}/search?k=$query")
            .document
            .getElementsByClass("list-films film-new")
            .firstOrNull()
            ?.select(Evaluator.AttributeWithValueStarting("class", "item"))
            ?.mapNotNull {
                it.toSearchResponse().also { response ->
                    LogUtils.d(response)
                }
            } ?: listOf()
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val path = if (request.data.contains("tuyen-tap")) "series" else "new"
        val homeItems = document.getElementsByClass("list-films film-$path")
            .firstOrNull()
            ?.select(Evaluator.AttributeWithValueStarting("class", "item"))
            ?.mapNotNull {
                it.toSearchResponse()
            } ?: listOf()
        return newHomePageResponse(request, homeItems)
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        return if (url.contains("tuyen-tap")) {
            loadCollections(document, url)
        } else {
            loadNormal(document, url)
        }
    }

    private suspend fun loadNormal(document: Document, url: String): LoadResponse? {
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
        val details = document.selectFirst(Evaluator.Class("detail"))
        val description =
            details?.selectFirst(Evaluator.Id("info-film"))?.selectFirst(Evaluator.Class("tab"))
                ?.selectFirst(Evaluator.Tag("p"))?.text()
        val tvType = if (document.select(Evaluator.ContainsText("TV Series")).isNotEmpty()) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
        val actors = document.selectFirst(Evaluator.Class("dienviendd"))
            ?.select(Evaluator.Tag("a"))?.map { it.text().trim() }
        val tags = document.selectFirst(Evaluator.Class("theloaidd"))
            ?.select(Evaluator.Tag("a"))?.map { it.text().trim() }
        val infoElement = document.selectFirst(Evaluator.Class("dinfo"))
        val info = infoElement?.toString()
            ?.replace("\n", "")
            ?.replace(" +".toRegex(), " ") ?: ""
        val year = info.substringAfter("Năm sản xuất")
            .substringBefore("</dd>")
            .toInteger()
        val status = info.substringAfter("Tình trạng")
            .substringBefore("</dd>")
        val rating = infoElement?.selectFirst(Evaluator.Tag("a"))?.text() ?: ""
        val recommendations = document.selectFirst(Evaluator.Id("film_related"))?.run {
            select(Evaluator.Tag("li")).map {
                val name = it.attr("title") ?: ""
                val relatedLink = it.selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
                val relatedImg = it.selectFirst(Evaluator.Tag("img"))?.attr("src") ?: ""
                LiveSearchResponse(
                    name = name,
                    url = fixUrl(relatedLink),
                    posterUrl = fixUrl(relatedImg),
                    type = TvType.Movie,
                    apiName = "BluPhim"
                )
            }
        } ?: listOf()
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
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating.toInteger()
                addActors(actors)
                addTrailer(youtubeTrailer)
                this.recommendations = recommendations
                this.showStatus = if (status.contains("Hoàn tất")) {
                    ShowStatus.Completed
                } else {
                    ShowStatus.Ongoing
                }
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(link)) {
                this.posterUrl = fixUrl(posterUrl)
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating.toInteger()
                addActors(actors)
                addTrailer(youtubeTrailer)
                this.recommendations = recommendations
            }
        }
    }

    private suspend fun loadCollections(document: Document, url: String): LoadResponse? {
        val title = document.selectFirst(Evaluator.Class("main-content"))
            ?.selectFirst(Evaluator.Tag("h1"))?.text() ?: ""
        val items = document.getElementsByClass("list-films film-new")
            .firstOrNull()
            ?.select(Evaluator.AttributeWithValueStarting("class", "item"))
        val episodes = items?.map {
            val href = it.selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
            val name = it.selectFirst(Evaluator.Class("name"))
                ?.selectFirst(Evaluator.Tag("span"))?.text() ?: ""
            val posterUrl = it.selectFirst(Evaluator.Tag("img"))?.attr("src") ?: ""
            Episode(
                data = "${fixUrl(href)}/series",
                name = name,
                posterUrl = fixUrl(posterUrl),
                description = it.selectFirst(Evaluator.Tag("span"))?.text()
            )
        } ?: listOf()
        val posterUrl = document.selectFirst(Evaluator.AttributeWithValue("property", "og:image"))
            ?.attr("content") ?: ""
        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = fixUrl(posterUrl)
            this.plot = title
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val isSeries = data.endsWith("/series")
        var link = data
        if (isSeries) {
            link = data.replaceFirst("/series", "")
            val doc = app.get(fixUrl(link)).document
            link = doc.selectFirst(Evaluator.Class("poster"))
                ?.selectFirst(Evaluator.Class("btn-see btn btn-danger btn-stream-link"))
                ?.attr("href") ?: ""
        }
        var document = app.get(fixUrl(link)).document

        val ref = document.selectFirst(Evaluator.Id("iframeStream"))?.attr("src")
            ?: return super.loadLinks(
                link,
                isCasting,
                subtitleCallback,
                callback
            )
        val refUri = Uri.parse(ref)
        document = app.get(
            url = fixUrl(ref),
            referer = mainUrl
        ).document
        val videoId = ref.substringAfter("id=").substringBefore("&")
        val script = document.select(Evaluator.Tag("script")).map { it.data() }
            .firstOrNull { it.contains("ShowLoading()") }
        if (!script.isNullOrBlank()) {
            val token = try {
                app.post(
                    url = "${refUri.scheme}://${refUri.host}/geturl",
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
                ).text
            } catch (e: Exception) {
                ""
            }

            val cdn = script.substringAfter("cdn = '").substringBefore("'")
            val cdnUrl = "$cdn/streaming?id=$videoId&web=BluPhim.Net&$token&cdn=$cdn&lang=vi"
            document = app.get(
                url = cdnUrl,
                referer = "${refUri.scheme}://${refUri.host}/",
            ).document
            val content = document.data()
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
                        referer = cdn,
                        quality = Qualities.P1080.value,
                        isM3u8 = true,
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
        val link = selectFirst(Evaluator.Attribute("href")) ?: return null
        val name = link.selectFirst(Evaluator.Class("name"))?.selectFirst("span")?.text() ?: ""
        val href = fixUrl(link.attr("href"))
        val img = link.selectFirst("img")?.attr("src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = href,
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "Blu phim"
        )
    }
}