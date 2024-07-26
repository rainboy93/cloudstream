package com.lagradost.cloudstream3.plugins.tmdb


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class TVSeriesResponse(
    @JsonProperty("adult")
    val adult: Boolean? = false,
    @JsonProperty("backdrop_path")
    val backdropPath: String? = "",
    @JsonProperty("created_by")
    val createdBy: List<Any?>? = listOf(),
    @JsonProperty("episode_run_time")
    val episodeRunTime: List<Int?>? = listOf(),
    @JsonProperty("first_air_date")
    val firstAirDate: String? = "",
    @JsonProperty("genres")
    val genres: List<Genre?>? = listOf(),
    @JsonProperty("homepage")
    val homepage: String? = "",
    @JsonProperty("id")
    val id: Int? = 0,
    @JsonProperty("in_production")
    val inProduction: Boolean? = false,
    @JsonProperty("languages")
    val languages: List<String?>? = listOf(),
    @JsonProperty("last_air_date")
    val lastAirDate: String? = "",
    @JsonProperty("last_episode_to_air")
    val lastEpisodeToAir: LastEpisodeToAir? = LastEpisodeToAir(),
    @JsonProperty("name")
    val name: String? = "",
    @JsonProperty("networks")
    val networks: List<Network?>? = listOf(),
    @JsonProperty("next_episode_to_air")
    val nextEpisodeToAir: NextEpisodeToAir? = NextEpisodeToAir(),
    @JsonProperty("number_of_episodes")
    val numberOfEpisodes: Int? = 0,
    @JsonProperty("number_of_seasons")
    val numberOfSeasons: Int? = 0,
    @JsonProperty("origin_country")
    val originCountry: List<String?>? = listOf(),
    @JsonProperty("original_language")
    val originalLanguage: String? = "",
    @JsonProperty("original_name")
    val originalName: String? = "",
    @JsonProperty("overview")
    val overview: String? = "",
    @JsonProperty("popularity")
    val popularity: Double? = 0.0,
    @JsonProperty("poster_path")
    val posterPath: String? = "",
    @JsonProperty("production_companies")
    val productionCompanies: List<ProductionCompany?>? = listOf(),
    @JsonProperty("production_countries")
    val productionCountries: List<ProductionCountry?>? = listOf(),
    @JsonProperty("seasons")
    val seasons: List<Season?>? = listOf(),
    @JsonProperty("spoken_languages")
    val spokenLanguages: List<SpokenLanguage?>? = listOf(),
    @JsonProperty("status")
    val status: String? = "",
    @JsonProperty("tagline")
    val tagline: String? = "",
    @JsonProperty("type")
    val type: String? = "",
    @JsonProperty("vote_average")
    val voteAverage: Double? = 0.0,
    @JsonProperty("vote_count")
    val voteCount: Int? = 0
) {
    @Keep
    data class Genre(
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("name")
        val name: String? = ""
    )

    @Keep
    data class LastEpisodeToAir(
        @JsonProperty("air_date")
        val airDate: String? = "",
        @JsonProperty("episode_number")
        val episodeNumber: Int? = 0,
        @JsonProperty("episode_type")
        val episodeType: String? = "",
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("overview")
        val overview: String? = "",
        @JsonProperty("production_code")
        val productionCode: String? = "",
        @JsonProperty("runtime")
        val runtime: Int? = 0,
        @JsonProperty("season_number")
        val seasonNumber: Int? = 0,
        @JsonProperty("show_id")
        val showId: Int? = 0,
        @JsonProperty("still_path")
        val stillPath: String? = "",
        @JsonProperty("vote_average")
        val voteAverage: Double? = 0.0,
        @JsonProperty("vote_count")
        val voteCount: Int? = 0
    )

    @Keep
    data class Network(
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("logo_path")
        val logoPath: String? = "",
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("origin_country")
        val originCountry: String? = ""
    )

    @Keep
    data class NextEpisodeToAir(
        @JsonProperty("air_date")
        val airDate: String? = "",
        @JsonProperty("episode_number")
        val episodeNumber: Int? = 0,
        @JsonProperty("episode_type")
        val episodeType: String? = "",
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("overview")
        val overview: String? = "",
        @JsonProperty("production_code")
        val productionCode: String? = "",
        @JsonProperty("runtime")
        val runtime: Int? = 0,
        @JsonProperty("season_number")
        val seasonNumber: Int? = 0,
        @JsonProperty("show_id")
        val showId: Int? = 0,
        @JsonProperty("still_path")
        val stillPath: String? = "",
        @JsonProperty("vote_average")
        val voteAverage: Double? = 0.0,
        @JsonProperty("vote_count")
        val voteCount: Int? = 0
    )

    @Keep
    data class ProductionCompany(
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("logo_path")
        val logoPath: String? = "",
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("origin_country")
        val originCountry: String? = ""
    )

    @Keep
    data class ProductionCountry(
        @JsonProperty("iso_3166_1")
        val iso31661: String? = "",
        @JsonProperty("name")
        val name: String? = ""
    )

    @Keep
    data class Season(
        @JsonProperty("air_date")
        val airDate: String? = "",
        @JsonProperty("episode_count")
        val episodeCount: Int? = 0,
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("overview")
        val overview: String? = "",
        @JsonProperty("poster_path")
        val posterPath: String? = "",
        @JsonProperty("season_number")
        val seasonNumber: Int? = 0,
        @JsonProperty("vote_average")
        val voteAverage: Double? = 0.0
    )

    @Keep
    data class SpokenLanguage(
        @JsonProperty("english_name")
        val englishName: String? = "",
        @JsonProperty("iso_639_1")
        val iso6391: String? = "",
        @JsonProperty("name")
        val name: String? = ""
    )
}