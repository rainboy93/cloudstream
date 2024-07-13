package com.lagradost.cloudstream3.plugins.vebo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WatchResponse(
    @SerializedName("data")
    val `data`: Data = Data(),
    @SerializedName("status")
    val status: Int = 0
) {
    @Keep
    data class Data(
        @SerializedName("highlight")
        val highlight: Match? = Match(),
        @SerializedName("limit")
        val limit: Int = 0,
        @SerializedName("list")
        val list: List<Match> = listOf(),
        @SerializedName("page")
        val page: Int = 0,
        @SerializedName("total")
        val total: Int = 0
    ) {
        @Keep
        data class Match(
            @SerializedName("category")
            val category: Category? = Category(),
            @SerializedName("created_at")
            val createdAt: String? = "",
            @SerializedName("description")
            val description: String? = "",
            @SerializedName("feature_image")
            val featureImage: String? = "",
            @SerializedName("id")
            val id: String? = "",
            @SerializedName("link")
            val link: String? = "",
            @SerializedName("name")
            val name: String? = "",
            @SerializedName("slug")
            val slug: String? = "",
            @SerializedName("updated_at")
            val updatedAt: String? = ""
        ) {
            @Keep
            data class Category(
                @SerializedName("description")
                val description: String? = "",
                @SerializedName("name")
                val name: String? = "",
                @SerializedName("slug")
                val slug: String? = ""
            )
        }
    }
}