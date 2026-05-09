package com.lagradost.cloudstream3.plugins.kisskh

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addPoster
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.network.CloudflareKiller
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

class KissKHProvider(val plugin: KissKHPlugin) : MainAPI() {
    override var lang = "en"
    override var name = "KissKH"
    override var mainUrl = "https://kisskh.id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)

    override val mainPage = mainPageOf(
        Pair("${mainUrl}/api/DramaList/LastUpdate?ispc=false", "Last Update"),
        Pair("${mainUrl}/api/DramaList/MostView", "Most Viewed"),
        Pair("${mainUrl}/api/DramaList/TopRating?ispc=false", "Top Rating"),
    )

    override val hasMainPage = true
    override val hasDownloadSupport = true

    private val cfKiller = CloudflareKiller()

    override suspend fun search(query: String): List<SearchResponse> {
        val text = app.get("${mainUrl}/api/DramaList/Search?q=${query}&type=0").text
        val items = tryParseJson<List<DramaItem>>(text) ?: return emptyList()
        return items.map { it.toSearchResponse() }
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest,
    ): HomePageResponse {
        val name = request.name
        if (page > 1) return newHomePageResponse(
            list = HomePageList(name, emptyList()),
            hasNext = false
        )

        val text = app.get(request.data).text
        val items = tryParseJson<List<DramaItem>>(text) ?: emptyList()

        val homePageList = HomePageList(
            name = name,
            list = items.map { it.toSearchResponse() },
            isHorizontalImages = true
        )

        return newHomePageResponse(list = homePageList, hasNext = false)
    }

    override suspend fun load(url: String): LoadResponse {
        try {
            val dramaId = url.substringAfterLast(".")
            val text = app.get("${mainUrl}/api/DramaList/Drama/${dramaId}?isq=false").text
            val drama = tryParseJson<DramaDetail>(text) ?: throw Exception("Parse error")

            val isSingle = drama.type == "Movie" || (drama.episodes?.size ?: 0) <= 1

            if (isSingle) {
                val dataUrl = if (drama.episodes?.isNotEmpty() == true) {
                    "${drama.episodes.first().id}"
                } else ""

                return newMovieLoadResponse(drama.title, url, TvType.Movie, dataUrl) {
                    this.plot = drama.description
                    addPoster(drama.thumbnail)
                }
            }

            val episodes = drama.episodes?.sortedBy { it.number }?.map { ep ->
                newEpisode(
                    url = "${ep.id}",
                    initializer = {
                        name = "Episode ${ep.number.toInt()}"
                    },
                    fix = false
                )
            } ?: emptyList()

            val tvType = if (drama.type == "Anime") TvType.Anime else TvType.TvSeries
            return newTvSeriesLoadResponse(drama.title, url, tvType, episodes) {
                this.plot = drama.description
                addPoster(drama.thumbnail)
            }
        } catch (_: Exception) {
        }

        return newMovieLoadResponse("Something went wrong!", url, TvType.Movie, "") {
            this.plot = "There's a problem loading this content."
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val episodeId = data

        try {
            val response = app.get(
                "${mainUrl}/api/DramaList/Episode/${episodeId}.png?err=false&ts=&time=",
                interceptor = cfKiller,
                referer = "${mainUrl}/"
            )
            val text = response.text

            val episodeData = tryParseJson<EpisodeResponse>(text)
            val videoUrl = episodeData?.video ?: episodeData?.thirdParty

            if (!videoUrl.isNullOrEmpty()) {
                val isM3u8 = videoUrl.contains(".m3u8")
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        url = videoUrl,
                        name = name,
                        initializer = {
                            referer = "${mainUrl}/"
                            quality = Qualities.Unknown.value
                            type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                        }
                    )
                )
            }
        } catch (_: Exception) {
        }

        try {
            val subText = app.get("${mainUrl}/api/Sub/${episodeId}").text
            val subs = tryParseJson<List<SubtitleItem>>(subText)
            subs?.forEach { sub ->
                subtitleCallback.invoke(
                    newSubtitleFile(
                        sub.label,
                        sub.src
                    )
                )
            }
        } catch (_: Exception) {
        }

        return true
    }

    private fun DramaItem.toSearchResponse(): SearchResponse {
        val dramaUrl = "${mainUrl}/Drama/${title?.replace(" ", "-")}.${id}"
        return newMovieSearchResponse(title ?: "", dramaUrl, TvType.TvSeries, true) {
            this.posterUrl = thumbnail
        }
    }

    data class DramaItem(
        @JsonProperty("id") val id: Int,
        @JsonProperty("title") val title: String?,
        @JsonProperty("thumbnail") val thumbnail: String?,
        @JsonProperty("episodesCount") val episodesCount: Int?,
        @JsonProperty("label") val label: String?,
    )

    data class DramaDetail(
        @JsonProperty("id") val id: Int,
        @JsonProperty("title") val title: String,
        @JsonProperty("description") val description: String?,
        @JsonProperty("thumbnail") val thumbnail: String?,
        @JsonProperty("type") val type: String?,
        @JsonProperty("country") val country: String?,
        @JsonProperty("status") val status: String?,
        @JsonProperty("episodes") val episodes: List<EpisodeItem>?,
    )

    data class EpisodeItem(
        @JsonProperty("id") val id: Int,
        @JsonProperty("number") val number: Double,
        @JsonProperty("sub") val sub: Int?,
    )

    data class EpisodeResponse(
        @JsonProperty("Video") val video: String?,
        @JsonProperty("ThirdParty") val thirdParty: String?,
    )

    data class SubtitleItem(
        @JsonProperty("src") val src: String,
        @JsonProperty("label") val label: String,
        @JsonProperty("land") val land: String?,
        @JsonProperty("default") val default: Boolean?,
    )
}
