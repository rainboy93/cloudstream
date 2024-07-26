package com.lagradost.cloudstream3.plugins.tmdb


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class SourceResponse(
    @JsonProperty("source")
    val source: String? = "",
    @JsonProperty("subtitles")
    val subtitles: List<Subtitle>? = listOf()
) {
    @Keep
    data class Subtitle(
        @JsonProperty("label")
        val label: String? = "",
        @JsonProperty("url")
        val url: String? = ""
    )
}