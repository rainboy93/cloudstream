package com.lagradost.cloudstream3.plugins.vuighe

import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LiveSearchResponse
import com.lagradost.cloudstream3.LoadResponse
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
import com.lagradost.cloudstream3.plugins.removeNonNumeric
import com.lagradost.cloudstream3.ui.player.PlayerSubtitleHelper.Companion.toSubtitleMimeType
import com.lagradost.cloudstream3.ui.player.SubtitleData
import com.lagradost.cloudstream3.ui.player.SubtitleOrigin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import java.io.File

class VuiGheProvider : MainAPI() {

    override var mainUrl = "https://vuighe3.com"
    override var name = "VuiGhe"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        MainPageData("BXH", "$mainUrl/bang-xep-hang/trang-", true),
        MainPageData("Anime", "$mainUrl/anime/trang-", true),
        MainPageData("Movie", "$mainUrl/movie/trang-", false),
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("${mainUrl}/tim-kiem/$query")
            .document
            .select(Evaluator.AttributeWithValueStarting("class", "group/film"))
            .mapNotNull {
                it.toSearchResponse()
            }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        if (request.name != "BXH" || page == 1) {
            val document = app.get(request.data + page).document
            val homeItems =
                document.select(
                    Evaluator.AttributeWithValueStarting(
                        "class",
                        "group/${if (request.name == "Movie") "movie" else "film"}"
                    )
                ).mapNotNull {
                    it.toSearchResponse()
                }
            return newHomePageResponse(request, homeItems)
        } else {
            return super.getMainPage(page, request)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val container = document.selectFirst(Evaluator.Class("container play")) ?: return null
        val fId = container.attr("data-id")
        val poster = document.selectFirst(Evaluator.Class("w-full rounded"))
            ?.attr("src") ?: ""
        val title = container.attr("data-name")
        val totalEpisode =
            document.selectFirst(Evaluator.AttributeWithValueStarting("class", "order-last"))
                ?.selectFirst(Evaluator.Tag("p"))?.text()
                .removeNonNumeric()
                .toIntOrNull() ?: -1
        val tvType = if (totalEpisode > 0) {
            TvType.TvSeries
        } else {
            TvType.Movie
        }
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
            val description =
                document.selectFirst(Evaluator.AttributeWithValueStarting("class", "order-last"))
                    ?.select(Evaluator.Tag("p"))?.lastOrNull()?.text() ?: ""
            val epsInfo = app.get(
                "$mainUrl/api/v2/films/$fId/episodes?sort=name",
                referer = url,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "X-Requested-With" to "XMLHttpRequest",
                )
            ).parsedSafe<FilmResponse>()?.data?.map { ep ->
                Episode(
                    data = fixUrl(ep.link ?: ""),
                    name = ep.detailName,
                    episode = ep.name,
                    posterUrl = fixUrl(ep.thumbnailMedium ?: "")
                )
            } ?: listOf()
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, epsInfo) {
                this.posterUrl = poster
                this.plot = description
                this.recommendations = recommendations
            }
        } else {
            val description =
                document.selectFirst(Evaluator.AttributeWithValueStarting("class", "order-last"))
                    ?.text() ?: ""
            newMovieLoadResponse(title, url, TvType.Movie, fixUrl(url)) {
                this.posterUrl = poster
                this.plot = description
                this.recommendations = recommendations
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
        subtitleFileCallback: (SubtitleData) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val container = document.selectFirst(Evaluator.Class("container play")) ?: return false
        val fId = container.attr("data-id")
        val eId = container.attr("data-episode-id")
        val response = try {
            app.get(
                "$mainUrl/api/v2/films/$fId/episodes/$eId/true",
                referer = data,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "X-Requested-With" to "XMLHttpRequest"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        var subtitleUrl = ""

        val sources = mutableListOf<Triple<String, String, Qualities>>()
        try {
            val jsonObject = JSONObject(response?.text ?: "")
            val videoSources = jsonObject.getJSONObject("sources")
            videoSources.optJSONArray("vip")?.run {
                for (i in 0 until length()) {
                    (get(i) as JSONObject).run {
                        val src = getString("src")
                        val type = getString("type")
                        val quality = getString("quality")
                        sources.add(Triple(src, type, Qualities.fromString(quality)))
                    }
                }
            }
            videoSources.optJSONObject("m3u8")?.run {
                try {
                    for (i in 0..128) {
                        val string = decodeString(getString("1"), i)
                        if (string.contains("m3u8")) {
                            sources.add(Triple(string, "m3u8", Qualities.Unknown))
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            jsonObject.optJSONObject("cue")?.run {
                try {
                    val subtitle = getString("vi")
                    if (!subtitle.isNullOrBlank()) {
                        val file = File(
                            PathUtils.getInternalAppFilesPath(),
                            "subtitles${File.separator}${fId}_${eId}.srt"
                        )
                        if (FileUtils.delete(file)) {
                            FileUtils.createOrExistsFile(file)
                            FileIOUtils.writeFileFromString(file, subtitle)
                            subtitleUrl = file.toURI().path
                            val name = file.name
                            subtitleFileCallback.invoke(
                                SubtitleData(
                                    name,
                                    subtitleUrl,
                                    SubtitleOrigin.DOWNLOADED_FILE,
                                    name.toSubtitleMimeType(),
                                    emptyMap(),
                                    "vi"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        sources.apmap { (link, source, quality) ->
            safeApiCall {
                callback.invoke(
                    ExtractorLink(
                        source,
                        source,
                        link,
                        referer = data,
                        quality = quality.value,
                        source.contains("m3u8", true) || source.contains("hls", true),
                        headers = mapOf("Referer" to mainUrl)
                    )
                )
            }
        }
        return true
    }

    private fun Element.toSearchResponse(): LiveSearchResponse? {
        val link = selectFirst(Evaluator.Tag("a")) ?: return null
        val name = link.selectFirst("img")?.attr("alt") ?: ""
        val href = fixUrl(link.attr("href"))
        val img = link.selectFirst("img")?.attr("data-src") ?: ""
        return LiveSearchResponse(
            name = name,
            url = href,
            posterUrl = fixUrl(img),
            type = TvType.Movie,
            apiName = "VuiGhe"
        )
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
}