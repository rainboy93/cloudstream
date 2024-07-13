package com.lagradost.cloudstream3.plugins.vebo


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WatchDetailResponse(
    @SerializedName("data")
    val `data`: Data? = Data(),
    @SerializedName("data_hot")
    val dataHot: List<Data?>? = listOf(),
    @SerializedName("data_related")
    val dataRelated: List<Data?>? = listOf(),
    @SerializedName("status")
    val status: Int? = 0
) {
    @Keep
    data class Data(
        @SerializedName("author")
        val author: Any? = Any(),
        @SerializedName("category")
        val category: Category? = Category(),
        @SerializedName("content")
        val content: String? = "",
        @SerializedName("created_at")
        val createdAt: String? = "",
        @SerializedName("description")
        val description: String? = "",
        @SerializedName("feature_image")
        val featureImage: String? = "",
        @SerializedName("h1")
        val h1: String? = "",
        @SerializedName("id")
        val id: String? = "",
        @SerializedName("name")
        val name: String? = "",
        @SerializedName("slug")
        val slug: String? = "",
        @SerializedName("title")
        val title: String? = "",
        @SerializedName("updated_at")
        val updatedAt: String? = "",
        @SerializedName("video_url")
        val videoUrl: String? = ""
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