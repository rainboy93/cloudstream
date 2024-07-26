package com.lagradost.cloudstream3.plugins

import com.blankj.utilcode.util.GsonUtils
import com.lagradost.cloudstream3.plugins.tmdb.TMDBProvider

fun String?.removeNonNumeric(): String {
    return this?.replace(Regex("[^0-9]"), "")?.trim() ?: ""
}

fun String?.toInteger(): Int {
    return this.removeNonNumeric().toIntOrNull() ?: -1
}

fun String.withExtraData(extraData: TMDBProvider.ExtraData): String {
    val extra = GsonUtils.toJson(extraData)
    return "${this}&extra=$extra"
}

fun String.removeExtra(): String {
    return this.substringBefore("&extra=")
}

fun String.getExtra(): TMDBProvider.ExtraData? {
    val extra = substringAfter("&extra=")
    return try {
        GsonUtils.fromJson(extra, TMDBProvider.ExtraData::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}