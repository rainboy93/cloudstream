package com.lagradost.cloudstream3.plugins.motchill

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Server(
    @SerializedName("IsFrame")
    val isFrame: Boolean = false,
    @SerializedName("Link")
    val link: String = "",
    @SerializedName("ServerName")
    val serverName: String = ""
)