package com.lagradost.cloudstream3.plugins.vebo


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LiveResponse(
    @SerializedName("data")
    val `data`: List<Data>? = listOf(),
    @SerializedName("status")
    val status: Int? = 0
) {
    @Keep
    data class Data(
        @SerializedName("away")
        val away: Team? = Team(),
        @SerializedName("away_red_cards")
        val awayRedCards: Int? = 0,
        @SerializedName("commentators")
        val commentators: List<Commentator>? = listOf(),
        @SerializedName("date")
        val date: String? = "",
        @SerializedName("has_lineup")
        val hasLineup: Boolean? = false,
        @SerializedName("has_tracker")
        val hasTracker: Boolean? = false,
        @SerializedName("home")
        val home: Team? = Team(),
        @SerializedName("home_red_cards")
        val homeRedCards: Int? = 0,
        @SerializedName("id")
        val id: String? = "",
        @SerializedName("is_featured")
        val isFeatured: Boolean? = false,
        @SerializedName("is_live")
        val isLive: Boolean? = false,
        @SerializedName("key_sync")
        val keySync: String? = "",
        @SerializedName("live_tracker")
        val liveTracker: String? = "",
        @SerializedName("match_status")
        val matchStatus: String? = "",
        @SerializedName("name")
        val name: String? = "",
        @SerializedName("parse_data")
        val parseData: ParseData? = ParseData(),
        @SerializedName("room_id")
        val roomId: String? = "",
        @SerializedName("scores")
        val scores: Scores? = Scores(),
        @SerializedName("slug")
        val slug: String? = "",
        @SerializedName("sport_type")
        val sportType: String? = "",
        @SerializedName("thumbnail_url")
        val thumbnailUrl: String? = "",
        @SerializedName("time_str")
        val timeStr: String? = "",
        @SerializedName("timestamp")
        val timestamp: Long? = 0,
        @SerializedName("tournament")
        val tournament: Tournament? = Tournament(),
        @SerializedName("win_code")
        val winCode: Int? = 0
    ) {
        @Keep
        data class Team(
            @SerializedName("gender")
            val gender: String? = "",
            @SerializedName("id")
            val id: String? = "",
            @SerializedName("logo")
            val logo: String? = "",
            @SerializedName("name")
            val name: String? = "",
            @SerializedName("name_code")
            val nameCode: String? = "",
            @SerializedName("short_name")
            val shortName: String? = "",
            @SerializedName("slug")
            val slug: String? = ""
        )

        @Keep
        data class Commentator(
            @SerializedName("avatar")
            val avatar: String? = "",
            @SerializedName("id")
            val id: String? = "",
            @SerializedName("name")
            val name: String? = "",
            @SerializedName("url")
            val url: String? = ""
        )

        @Keep
        data class ParseData(
            @SerializedName("status")
            val status: String? = "",
            @SerializedName("time")
            val time: String? = ""
        )

        @Keep
        data class Scores(
            @SerializedName("away")
            val away: Int? = 0,
            @SerializedName("detail")
            val detail: String? = null,
            @SerializedName("home")
            val home: Int? = 0,
            @SerializedName("sport_type")
            val sportType: String? = ""
        )

        @Keep
        data class Tournament(
            @SerializedName("logo")
            val logo: String? = "",
            @SerializedName("name")
            val name: String? = "",
            @SerializedName("priority")
            val priority: Int? = 0,
            @SerializedName("unique_tournament")
            val uniqueTournament: UniqueTournament? = UniqueTournament()
        ) {
            @Keep
            data class UniqueTournament(
                @SerializedName("id")
                val id: String? = "",
                @SerializedName("is_featured")
                val isFeatured: Boolean? = false,
                @SerializedName("logo")
                val logo: String? = "",
                @SerializedName("name")
                val name: String? = "",
                @SerializedName("priority")
                val priority: Int? = 0,
                @SerializedName("slug")
                val slug: String? = ""
            )
        }
    }
}