package com.lagradost.cloudstream3.plugins.thuviencine

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
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
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import java.lang.reflect.Type

class MotChillProvider(val plugin: MotChillPlugin) : MainAPI() {

    private val gson = Gson()

    override var mainUrl: String = "https://motchilltv.us"

    override var name: String = "MotChill"

    override val hasMainPage: Boolean = true

    override var lang: String = "vi"

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/phim-le?countryId&categoryId&typeId=single&year=&orderBy=UpdateOn&page=" to "Phim lẻ",
        "$mainUrl/phim-bo?countryId&categoryId&typeId=series&year=&orderBy=UpdateOn&page=" to "Phim bộ",
        "$mainUrl/quoc-gia/han-quoc?countryId=13&categoryId&typeId=&year=&orderBy=UpdateOn&page=" to "Phim Hàn Quốc",
        "$mainUrl/quoc-gia/trung-quoc?countryId=1&categoryId&typeId=&year=&orderBy=UpdateOn&page=" to "Phim Trung Quốc",
        "$mainUrl/quoc-gia/au-my?countryId=14&categoryId&typeId=&year=&orderBy=UpdateOn&page=" to "Phim Âu Mỹ"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        val document = app.get(request.data + page).document
        val homes = document.select(Evaluator.Tag("article")).map {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, homes)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val link = "$mainUrl/search?searchText=$query"
        val document = app.get(link).document
        return document.select(Evaluator.Tag("article")).map {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val main = document.selectFirst(Evaluator.Tag("main")) ?: return super.load(url)

        val title = main.selectFirst(Evaluator.Tag("h1"))?.text() ?: ""
        val link = fixUrl(
            main.selectFirst(Evaluator.Class("flex gap-x-2 items-center"))
                ?.selectFirst(Evaluator.Tag("a"))?.attr("href") ?: ""
        )
        val poster = main.selectFirst(Evaluator.Tag("img"))?.attr("src")
        val year = main.select(Evaluator.Class("text-zinc-400")).text().trim().toIntOrNull()
        val tvType =
            if (main.selectFirst(
                    Evaluator.AttributeWithValueContaining(
                        "href",
                        "phim-le"
                    )
                ) != null
            ) TvType.Movie else TvType.TvSeries
        val description = ""
        val trailer = ""
        val rating = 5
        return if (tvType == TvType.TvSeries) {
            val episodes =
                main.selectFirst(Evaluator.AttributeWithValueContaining("class", "pt-3 flex"))
                    ?.select(Evaluator.Tag("a"))?.map {
                        val href = fixUrl(it.attr("href"))
                        val episode = it.text().replace(Regex("[^0-9]"), "").trim().toIntOrNull()
                        val name = "Episode $episode"
                        Episode(
                            data = href,
                            name = name,
                            episode = episode,
                        )
                    } ?: listOf()
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.rating = rating
                addTrailer(trailer)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, link) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.rating = rating
                addTrailer(trailer)
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
        val script =
            document.selectFirst(Evaluator.AttributeWithValueContaining("type", "application/json"))
                ?.data()
                ?: return super.loadLinks(data, isCasting, subtitleCallback, callback)
        val contents = try {
            val jsonArray = JSONArray(script)
            var index = (jsonArray.get(0) as JSONArray).get(1).toString().toInt()
            var jsonObject = JSONObject(jsonArray[index].toString())
            index = jsonObject.get("data").toString().toInt()
            jsonObject = JSONObject(jsonArray[index].toString())
            index = jsonObject.get("episode").toString().toInt()
            jsonObject = JSONObject(jsonArray[index].toString())
            index = jsonObject.get("episode").toString().toInt()
            jsonObject = JSONObject(jsonArray[index].toString())
            val movieId = jsonObject.get("ProductId").toString().toInt()
            val episodeId = jsonObject.get("Id").toString().toInt()
            Pair(jsonArray.get(movieId), jsonArray.get(episodeId))
        } catch (e: Exception) {
            e.printStackTrace()
            return super.loadLinks(data, isCasting, subtitleCallback, callback)
        }
        val movieId = contents.first
        val episodeId = contents.second
        val response =
            app.get("https://motchilltv.us/api/play/get?movieId=$movieId&episodeId=$episodeId&server=0")
        val type: Type = object : TypeToken<List<Server>?>() {}.type
        val servers: List<Server> = try {
            gson.fromJson(response.text, type)
        } catch (e: Exception) {
            return super.loadLinks(data, isCasting, subtitleCallback, callback)
        }
        servers.apmap {
            safeApiCall {
                callback.invoke(
                    ExtractorLink(
                        it.serverName,
                        it.serverName,
                        it.link,
                        referer = "$mainUrl/",
                        quality = Qualities.P1080.value,
                        isM3u8 = true
                    )
                )
            }
        }
        return true
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title = select(Evaluator.Tag("a")).lastOrNull()?.attr("title") ?: ""
        val href = fixUrl(select(Evaluator.Tag("a")).lastOrNull()?.attr("href") ?: "")
        val posterUrl = fixUrl(selectFirst(Evaluator.Tag("img"))?.attr("src") ?: "")
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }
}