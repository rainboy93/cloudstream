package com.lagradost.cloudstream3.plugins.tmdb


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class TVSeasonsDetailResponse(
    @JsonProperty("air_date")
    val airDate: String? = "",
    @JsonProperty("episodes")
    val episodes: List<Episode>? = listOf(),
    @JsonProperty("_id")
    val _id: String? = "",
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
    val voteAverage: String? = ""
) {
    @Keep
    data class Episode(
        @JsonProperty("air_date")
        val airDate: String? = "",
        @JsonProperty("crew")
        val crew: List<Crew>? = listOf(),
        @JsonProperty("episode_number")
        val episodeNumber: Int? = 0,
        @JsonProperty("episode_type")
        val episodeType: String? = "",
        @JsonProperty("guest_stars")
        val guestStars: List<GuestStar>? = listOf(),
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
    ) {
        @Keep
        data class Crew(
            @JsonProperty("adult")
            val adult: Boolean? = false,
            @JsonProperty("credit_id")
            val creditId: String? = "",
            @JsonProperty("department")
            val department: String? = "",
            @JsonProperty("gender")
            val gender: Int? = 0,
            @JsonProperty("id")
            val id: Int? = 0,
            @JsonProperty("job")
            val job: String? = "",
            @JsonProperty("known_for_department")
            val knownForDepartment: String? = "",
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("original_name")
            val originalName: String? = "",
            @JsonProperty("popularity")
            val popularity: Double? = 0.0,
            @JsonProperty("profile_path")
            val profilePath: String? = ""
        )

        @Keep
        data class GuestStar(
            @JsonProperty("adult")
            val adult: Boolean? = false,
            @JsonProperty("character")
            val character: String? = "",
            @JsonProperty("credit_id")
            val creditId: String? = "",
            @JsonProperty("gender")
            val gender: Int? = 0,
            @JsonProperty("id")
            val id: Int? = 0,
            @JsonProperty("known_for_department")
            val knownForDepartment: String? = "",
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("order")
            val order: Int? = 0,
            @JsonProperty("original_name")
            val originalName: String? = "",
            @JsonProperty("popularity")
            val popularity: Double? = 0.0,
            @JsonProperty("profile_path")
            val profilePath: String? = ""
        )
    }
}