package com.lagradost.cloudstream3.syncproviders.providers

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.subtitles.AbstractSubProvider
import com.lagradost.cloudstream3.subtitles.AbstractSubtitleEntities

class SubDLAPI : AbstractSubProvider {

    override val idPrefix: String = "subdl"

    override suspend fun search(query: AbstractSubtitleEntities.SubtitleSearch): List<AbstractSubtitleEntities.SubtitleEntity>? {
        val dataMap = mutableMapOf<String, String>(
            "api_key" to "m9_VsNo2DLkRomWNW06u0hwPmm5ndiRx",
            "languages" to (query.lang?.uppercase() ?: "VI"),
            "film_name" to query.query,
        ).apply {
            if (query.imdb != null) {
                this["imdb_id"] = query.imdb.toString()
            }
            if (query.seasonNumber != null) {
                this["season_number"] = query.seasonNumber.toString()
            }
            if (query.epNumber != null) {
                this["episode_number"] = query.epNumber.toString()
            }
            if (query.year != null) {
                this["year"] = query.year.toString()
            }
        }
        val response = app.get(
            url = "https://api.subdl.com/api/v1/subtitles",
            params = dataMap
        ).parsedSafe<SubDLResponse>()
        return response?.subtitles?.map {
            AbstractSubtitleEntities.SubtitleEntity(
                idPrefix = idPrefix,
                name = it.name ?: "",
                source = it.name ?: "",
                data = it.url ?: "",
                lang = query.lang ?: "vi",
                epNumber = query.epNumber
            )
        }
    }

    override suspend fun load(data: AbstractSubtitleEntities.SubtitleEntity): String? {
        return data.data
    }

    @Keep
    data class SubDLResponse(
        @JsonProperty("results")
        val results: List<Result>? = listOf(),
        @JsonProperty("status")
        val status: Boolean? = false,
        @JsonProperty("subtitles")
        val subtitles: List<Subtitle>? = listOf()
    ) {
        @Keep
        data class Result(
            @JsonProperty("first_air_date")
            val firstAirDate: String? = "",
            @JsonProperty("imdb_id")
            val imdbId: String? = "",
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("release_date")
            val releaseDate: String? = "",
            @JsonProperty("sd_id")
            val sdId: Int? = 0,
            @JsonProperty("slug")
            val slug: String? = "",
            @JsonProperty("tmdb_id")
            val tmdbId: Int? = 0,
            @JsonProperty("type")
            val type: String? = "",
            @JsonProperty("year")
            val year: Int? = 0
        )

        @Keep
        data class Subtitle(
            @JsonProperty("author")
            val author: String? = "",
            @JsonProperty("episode")
            val episode: Int? = 0,
            @JsonProperty("episode_end")
            val episodeEnd: Int? = 0,
            @JsonProperty("episode_from")
            val episodeFrom: Int? = 0,
            @JsonProperty("full_season")
            val fullSeason: Boolean? = false,
            @JsonProperty("hi")
            val hi: Boolean? = false,
            @JsonProperty("lang")
            val lang: String? = "",
            @JsonProperty("language")
            val language: String? = "",
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("release_name")
            val releaseName: String? = "",
            @JsonProperty("season")
            val season: Int? = 0,
            @JsonProperty("subtitlePage")
            val subtitlePage: String? = "",
            @JsonProperty("url")
            val url: String? = ""
        )
    }
}