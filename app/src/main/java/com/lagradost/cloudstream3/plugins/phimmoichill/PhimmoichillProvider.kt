package com.lagradost.cloudstream3.plugins.phimmoichill

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addQuality
import com.lagradost.cloudstream3.addSub
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPage
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.plugins.removeNonNumeric
import com.lagradost.cloudstream3.plugins.toInteger
import com.lagradost.cloudstream3.toRatingInt
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkPlayList
import com.lagradost.cloudstream3.utils.PlayListItem
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import java.net.URLDecoder

class PhimmoichillProvider : MainAPI() {
    override var mainUrl = "https://phimmoichillv.net"
    override var name = "Phimmoichill"
    override val hasMainPage = true
    override var lang = "vi"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
        TvType.AsianDrama
    )

    override val mainPage = mainPageOf(
        mainPage("$mainUrl/list/phim-le/page-", "Phim Lẻ", true),
        mainPage("$mainUrl/list/phim-bo/page-", "Phim Bộ", true),
        mainPage("$mainUrl/list/phim-netflix/page-", "Phim Netflix", true),
        mainPage("$mainUrl/list/phim-dc/page-", "Phim DC Comic", true),
        mainPage("$mainUrl/list/phim-marvel/page-", "Phim Marvel", true),
        mainPage("$mainUrl/country/phim-han-quoc/page-", "Phim Hàn Quốc", true),
        mainPage("$mainUrl/country/phim-trung-quoc/page-", "Phim Trung Quốc", true),
        mainPage("$mainUrl/country/phim-thai-lan/page-", "Phim Thái Lan", true),
        mainPage("$mainUrl/genre/phim-hoat-hinh/page-", "Phim Hoạt Hình", true),
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("li.item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request, home)
    }

    private fun decode(input: String): String? = URLDecoder.decode(input, "utf-8")

    private fun Element.toSearchResult(): SearchResponse {
        val title = this.selectFirst("p,h3")?.text()?.trim().toString()
        val href = fixUrl(this.selectFirst("a")!!.attr("href"))
        val posterUrl = this.selectFirst("img")
            ?.attr("src")
            ?.run {
                decode(this)?.ifBlank {
                    this@toSearchResult.selectFirst("img")?.attr("data-src") ?: ""
                }
            } ?: ""
        val temp = this.select("span.label").text()
        val year = this.selectFirst(Evaluator.Class("label-quality"))?.text()?.toInteger()
        return if (temp.contains(Regex("\\d"))) {
            val episode = Regex("(\\((\\d+))|(\\s(\\d+))").find(temp)?.groupValues?.map { num ->
                num.replace(Regex("\\(|\\s"), "")
            }?.distinct()?.firstOrNull()?.toIntOrNull()
            newAnimeSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addSub(episode)
                this.year = year
            }
        } else {
            val quality =
                temp.replace(Regex("(-.*)|(\\|.*)|(?i)(VietSub.*)|(?i)(Thuyết.*)"), "").trim()
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
                this.year = year
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val link = "$mainUrl/tim-kiem/$query"
        val document = app.get(link).document

        return document.select("ul.list-film li").map {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1[itemprop=name]")?.text()?.trim().toString()
        val link = document.select("ul.list-button li:last-child a").attr("href")
        val poster = document.selectFirst("div.image img[itemprop=image]")?.attr("src")
        val tags = document.select("ul.entry-meta.block-film li:nth-child(4) a").map { it.text() }
        val year = document.select("ul.entry-meta.block-film li:nth-child(2) a").text().trim()
            .toIntOrNull()
        val tvType = if (document.select("div.latest-episode").isNotEmpty()
        ) TvType.TvSeries else TvType.Movie
        val description = document.select("div#film-content").text().trim()
        val trailer =
            document.select("div#trailer script").last()?.data()?.substringAfter("file: \"")
                ?.substringBefore("\",")
        val rating =
            document.select("ul.entry-meta.block-film li:nth-child(7) span").text().toRatingInt()
        val actors = document.select("ul.entry-meta.block-film li:last-child a").map { it.text() }
        val recommendations = document.select("ul#list-film-realted li.item").map {
            it.toSearchResult()
        }

        return if (tvType == TvType.TvSeries) {
            val docEpisodes = app.get(link).document
            val episodes = docEpisodes.select("ul#list_episodes > li").map {
                val href = it.select("a").attr("href")
                val episode =
                    it.select("a").text().replace(Regex("[^0-9]"), "").trim().toIntOrNull()
                val name = "Episode $episode"
                Episode(
                    data = href,
                    name = name,
                    episode = episode,
                    posterUrl = poster
                )
            }
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
                this.recommendations = recommendations
                addTrailer(trailer)
            }
        } else {
            val duration = document.select("ul.entry-meta.block-film li:nth-child(7)").text()
                .trim()
                .split("giờ")
                .run {
                    when (size) {
                        2 -> {
                            val hour = this[0].trim().toIntOrNull() ?: return@run null
                            val minute = this[1].removeNonNumeric().toIntOrNull() ?: return@run null
                            hour * 60 + minute
                        }

                        1 -> {
                            this[0].removeNonNumeric().toIntOrNull() ?: return@run null
                        }

                        else -> {
                            null
                        }
                    }
                }
            newMovieLoadResponse(title, url, TvType.Movie, link) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
                this.recommendations = recommendations
                addTrailer(trailer)
                this.duration = duration
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

        val key = document.select("div#content script").firstNotNullOf { script ->
            if (script.data().contains("filmInfo.episodeID =")) {
                val id = script.data().substringAfter("filmInfo.episodeID = parseInt('")
                    .substringBefore("');")
                app.post(
                    // Not mainUrl
                    url = "$mainUrl/chillsplayer.php",
                    data = mapOf("qcao" to id),
                    referer = data,
                    headers = mapOf(
                        "X-Requested-With" to "XMLHttpRequest",
                        "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8"
                    )
                ).text.also { println("HERERERR $it") }.substringAfterLast("iniPlayers(\"")
                    .substringBefore("\",")
            } else {
                null
            }
        }

        listOf(
            Pair("https://so-trym.topphimmoi.org/hlspm/$key", "PMFAST"),
            Pair("https://dash.megacdn.xyz/hlspm/$key", "PMHLS"),
            Pair("https://dash.megacdn.xyz/dast/$key/index.m3u8", "MEGA"),
            Pair("https://dash.motchills.net/raw/$key/index.m3u8", "PMBK"),
        ).apmap { (link, source) ->
            safeApiCall {
                if (source == "PMBK" || source == "MEGA") {
                    callback.invoke(
                        ExtractorLink(
                            source,
                            source,
                            link,
                            referer = "$mainUrl/",
                            quality = Qualities.P1080.value,
                            isM3u8 = true
                        )
                    )
                } else {
                    val playList = app.get(link, referer = "$mainUrl/")
                        .parsedSafe<ResponseM3u>()?.main?.segments?.map { segment ->
                            PlayListItem(
                                segment.link,
                                (segment.du.toFloat() * 1_000_000).toLong()
                            )
                        }

                    callback.invoke(
                        ExtractorLinkPlayList(
                            source,
                            source,
                            playList ?: return@safeApiCall,
                            referer = "$mainUrl/",
                            quality = Qualities.P1080.value,
                            headers = mapOf(
//                                "If-None-Match" to "*",
                                "Origin" to mainUrl,
                            )
                        )
                    )
                }
            }
        }
        return true
    }

    data class Segment(
        @JsonProperty("du") val du: String,
        @JsonProperty("link") val link: String,
    )

    data class DataM3u(
        @JsonProperty("segments") val segments: List<Segment>?,
    )

    data class ResponseM3u(
        @JsonProperty("2048p") val main: DataM3u?,
    )

}