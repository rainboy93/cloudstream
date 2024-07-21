package com.lagradost.cloudstream3.plugins.ophim


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class MoviesResponse(
    @JsonProperty("data")
    val `data`: Data? = Data(),
    @JsonProperty("message")
    val message: String? = "",
    @JsonProperty("status")
    val status: String? = ""
) {
    @Keep
    data class Data(
        @JsonProperty("APP_DOMAIN_CDN_IMAGE")
        val aPPDOMAINCDNIMAGE: String? = "",
        @JsonProperty("APP_DOMAIN_FRONTEND")
        val aPPDOMAINFRONTEND: String? = "",
        @JsonProperty("breadCrumb")
        val breadCrumb: List<BreadCrumb?>? = listOf(),
        @JsonProperty("items")
        val items: List<Item?>? = listOf(),
        @JsonProperty("params")
        val params: Params? = Params(),
        @JsonProperty("seoOnPage")
        val seoOnPage: SeoOnPage? = SeoOnPage(),
        @JsonProperty("titlePage")
        val titlePage: String? = "",
        @JsonProperty("type_list")
        val typeList: String? = ""
    ) {
        @Keep
        data class BreadCrumb(
            @JsonProperty("isCurrent")
            val isCurrent: Boolean? = false,
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("position")
            val position: Int? = 0,
            @JsonProperty("slug")
            val slug: String? = ""
        )

        @Keep
        data class Item(
            @JsonProperty("category")
            val category: List<Category?>? = listOf(),
            @JsonProperty("chieurap")
            val chieurap: Boolean? = false,
            @JsonProperty("country")
            val country: List<Country?>? = listOf(),
            @JsonProperty("episode_current")
            val episodeCurrent: String? = "",
            @JsonProperty("_id")
            val id: String? = "",
            @JsonProperty("imdb")
            val imdb: Imdb? = Imdb(),
            @JsonProperty("lang")
            val lang: String? = "",
            @JsonProperty("modified")
            val modified: Modified? = Modified(),
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("origin_name")
            val originName: String? = "",
            @JsonProperty("poster_url")
            val posterUrl: String? = "",
            @JsonProperty("quality")
            val quality: String? = "",
            @JsonProperty("slug")
            val slug: String? = "",
            @JsonProperty("sub_docquyen")
            val subDocquyen: Boolean? = false,
            @JsonProperty("thumb_url")
            val thumbUrl: String? = "",
            @JsonProperty("time")
            val time: String? = "",
            @JsonProperty("tmdb")
            val tmdb: Tmdb? = Tmdb(),
            @JsonProperty("type")
            val type: String? = "",
            @JsonProperty("year")
            val year: Int? = 0
        ) {
            @Keep
            data class Category(
                @JsonProperty("id")
                val id: String? = "",
                @JsonProperty("name")
                val name: String? = "",
                @JsonProperty("slug")
                val slug: String? = ""
            )

            @Keep
            data class Country(
                @JsonProperty("id")
                val id: String? = "",
                @JsonProperty("name")
                val name: String? = "",
                @JsonProperty("slug")
                val slug: String? = ""
            )

            @Keep
            data class Imdb(
                @JsonProperty("id")
                val id: String? = ""
            )

            @Keep
            data class Modified(
                @JsonProperty("time")
                val time: String? = ""
            )

            @Keep
            data class Tmdb(
                @JsonProperty("id")
                val id: String? = "",
                @JsonProperty("season")
                val season: Int? = 0,
                @JsonProperty("type")
                val type: String? = "",
                @JsonProperty("vote_average")
                val voteAverage: Double? = 0.0,
                @JsonProperty("vote_count")
                val voteCount: Int? = 0
            )
        }

        @Keep
        data class Params(
            @JsonProperty("filterCategory")
            val filterCategory: List<String?>? = listOf(),
            @JsonProperty("filterCountry")
            val filterCountry: List<String?>? = listOf(),
            @JsonProperty("filterType")
            val filterType: String? = "",
            @JsonProperty("filterYear")
            val filterYear: String? = "",
            @JsonProperty("pagination")
            val pagination: Pagination? = Pagination(),
            @JsonProperty("sortField")
            val sortField: String? = "",
            @JsonProperty("sortType")
            val sortType: String? = "",
            @JsonProperty("type_slug")
            val typeSlug: String? = ""
        ) {
            @Keep
            data class Pagination(
                @JsonProperty("currentPage")
                val currentPage: Int? = 0,
                @JsonProperty("pageRanges")
                val pageRanges: Int? = 0,
                @JsonProperty("totalItems")
                val totalItems: Int? = 0,
                @JsonProperty("totalItemsPerPage")
                val totalItemsPerPage: Int? = 0
            )
        }

        @Keep
        data class SeoOnPage(
            @JsonProperty("descriptionHead")
            val descriptionHead: String? = "",
            @JsonProperty("og_image")
            val ogImage: List<String?>? = listOf(),
            @JsonProperty("og_type")
            val ogType: String? = "",
            @JsonProperty("og_url")
            val ogUrl: String? = "",
            @JsonProperty("titleHead")
            val titleHead: String? = ""
        )
    }
}