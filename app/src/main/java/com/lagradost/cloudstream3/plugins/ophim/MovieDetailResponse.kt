package com.lagradost.cloudstream3.plugins.ophim


import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class MovieDetailResponse(
    @JsonProperty("data")
    val `data`: Data? = Data(),
    @JsonProperty("message")
    val message: String? = "",
    @JsonProperty("status")
    val status: String? = ""
) {
    @Keep
    data class Data(
        @JsonProperty("breadCrumb")
        val breadCrumb: List<BreadCrumb?>? = listOf(),
        @JsonProperty("item")
        val item: Item? = Item(),
        @JsonProperty("params")
        val params: Params? = Params(),
        @JsonProperty("seoOnPage")
        val seoOnPage: SeoOnPage? = SeoOnPage()
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
            @JsonProperty("actor")
            val actor: List<String?>? = listOf(),
            @JsonProperty("category")
            val category: List<Category?>? = listOf(),
            @JsonProperty("chieurap")
            val chieurap: Boolean? = false,
            @JsonProperty("content")
            val content: String? = "",
            @JsonProperty("country")
            val country: List<Country?>? = listOf(),
            @JsonProperty("created")
            val created: Created? = Created(),
            @JsonProperty("director")
            val director: List<String?>? = listOf(),
            @JsonProperty("episode_current")
            val episodeCurrent: String? = "",
            @JsonProperty("episode_total")
            val episodeTotal: String? = "",
            @JsonProperty("episodes")
            val episodes: List<Episode?>? = listOf(),
            @JsonProperty("_id")
            val id: String? = "",
            @JsonProperty("imdb")
            val imdb: Imdb? = Imdb(),
            @JsonProperty("is_copyright")
            val isCopyright: Boolean? = false,
            @JsonProperty("lang")
            val lang: String? = "",
            @JsonProperty("modified")
            val modified: Modified? = Modified(),
            @JsonProperty("name")
            val name: String? = "",
            @JsonProperty("notify")
            val notify: String? = "",
            @JsonProperty("origin_name")
            val originName: String? = "",
            @JsonProperty("poster_url")
            val posterUrl: String? = "",
            @JsonProperty("quality")
            val quality: String? = "",
            @JsonProperty("showtimes")
            val showtimes: String? = "",
            @JsonProperty("slug")
            val slug: String? = "",
            @JsonProperty("status")
            val status: String? = "",
            @JsonProperty("sub_docquyen")
            val subDocquyen: Boolean? = false,
            @JsonProperty("thumb_url")
            val thumbUrl: String? = "",
            @JsonProperty("time")
            val time: String? = "",
            @JsonProperty("tmdb")
            val tmdb: Tmdb? = Tmdb(),
            @JsonProperty("trailer_url")
            val trailerUrl: String? = "",
            @JsonProperty("type")
            val type: String? = "",
            @JsonProperty("view")
            val view: Int? = 0,
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
            data class Created(
                @JsonProperty("time")
                val time: String? = ""
            )

            @Keep
            data class Episode(
                @JsonProperty("server_data")
                val serverData: List<ServerData?>? = listOf(),
                @JsonProperty("server_name")
                val serverName: String? = ""
            ) {
                @Keep
                data class ServerData(
                    @JsonProperty("filename")
                    val filename: String? = "",
                    @JsonProperty("link_embed")
                    val linkEmbed: String? = "",
                    @JsonProperty("link_m3u8")
                    val linkM3u8: String? = "",
                    @JsonProperty("name")
                    val name: String? = "",
                    @JsonProperty("slug")
                    val slug: String? = ""
                )
            }

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
                val season: Any? = Any(),
                @JsonProperty("type")
                val type: String? = "",
                @JsonProperty("vote_average")
                val voteAverage: Int? = 0,
                @JsonProperty("vote_count")
                val voteCount: Int? = 0
            )
        }

        @Keep
        data class Params(
            @JsonProperty("slug")
            val slug: String? = ""
        )

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
            @JsonProperty("seoSchema")
            val seoSchema: SeoSchema? = SeoSchema(),
            @JsonProperty("titleHead")
            val titleHead: String? = "",
            @JsonProperty("updated_time")
            val updatedTime: Long? = 0
        ) {
            @Keep
            data class SeoSchema(
                @JsonProperty("@context")
                val context: String? = "",
                @JsonProperty("dateCreated")
                val dateCreated: String? = "",
                @JsonProperty("dateModified")
                val dateModified: String? = "",
                @JsonProperty("datePublished")
                val datePublished: String? = "",
                @JsonProperty("director")
                val director: String? = "",
                @JsonProperty("image")
                val image: String? = "",
                @JsonProperty("name")
                val name: String? = "",
                @JsonProperty("@type")
                val type: String? = "",
                @JsonProperty("url")
                val url: String? = ""
            )
        }
    }
}