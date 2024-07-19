package com.lagradost.cloudstream3.plugins.vuighe


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class FilmResponse(
    @JsonProperty("data")
    val `data`: List<Data>? = listOf()
) {
    @Keep
    data class Data(
        @JsonProperty("detail_name")
        val detailName: String? = "",
        @JsonProperty("film_name")
        val filmName: String? = "",
        @JsonProperty("full_name")
        val fullName: String? = "",
        @JsonProperty("id")
        val id: Int? = 0,
        @JsonProperty("link")
        val link: String? = "",
        @JsonProperty("name")
        val name: Int? = 0,
        @JsonProperty("slug")
        val slug: String? = "",
        @JsonProperty("special_name")
        val specialName: Int? = 0,
        @JsonProperty("thumbnail_medium")
        val thumbnailMedium: String? = "",
        @JsonProperty("thumbnail_small")
        val thumbnailSmall: String? = "",
        @JsonProperty("views")
        val views: Int? = 0
    )
}