package com.lagradost.cloudstream3.plugins.vuighe

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EpisodeResponse(
    @SerializedName("detail_name")
    val detailName: String? = "",
    @SerializedName("film_name")
    val filmName: String? = "",
    @SerializedName("full_name")
    val fullName: String? = "",
    @SerializedName("has_preview")
    val hasPreview: Int? = 0,
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("is_copyrighted")
    val isCopyrighted: Int? = 0,
    @SerializedName("link")
    val link: String? = "",
    @SerializedName("midroll")
    val midroll: Int? = 0,
    @SerializedName("midroll2")
    val midroll2: Int? = 0,
    @SerializedName("name")
    val name: Int? = 0,
    @SerializedName("server")
    val server: Int? = 0,
    @SerializedName("slug")
    val slug: String? = "",
    @SerializedName("special_name")
    val specialName: Int? = 0,
    @SerializedName("thumbnail_medium")
    val thumbnailMedium: String? = "",
    @SerializedName("thumbnail_small")
    val thumbnailSmall: String? = "",
    @SerializedName("views")
    val views: Int? = 0
)