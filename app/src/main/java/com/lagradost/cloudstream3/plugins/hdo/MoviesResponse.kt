package com.lagradost.cloudstream3.plugins.hdo


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class MoviesResponse(
    @JsonProperty("page")
    val page: Int? = 0,
    @JsonProperty("results")
    val movies: List<Movie>? = listOf(),
    @JsonProperty("total_pages")
    val totalPages: Int? = 0,
    @JsonProperty("total_results")
    val totalResults: Int? = 0
) {
    @Keep
    data class Movie(
        @JsonProperty("adult")
        val adult: Boolean? = false,
        @JsonProperty("backdrop_path")
        val backdropPath: String? = "",
        @JsonProperty("genre_ids")
        val genreIds: List<Int?>? = listOf(),
        @JsonProperty("id")
        val id: Int = 0,
        @JsonProperty("original_language")
        val originalLanguage: String? = "",
        @JsonProperty("original_title")
        val originalTitle: String? = "",
        @JsonProperty("overview")
        val overview: String? = "",
        @JsonProperty("popularity")
        val popularity: Double? = 0.0,
        @JsonProperty("poster_path")
        val posterPath: String? = "",
        @JsonProperty("release_date")
        val releaseDate: String? = "",
        @JsonProperty("title")
        val title: String? = "",
        @JsonProperty("video")
        val video: Boolean? = false,
        @JsonProperty("vote_average")
        val voteAverage: Double? = 0.0,
        @JsonProperty("vote_count")
        val voteCount: Int? = 0
    )
}