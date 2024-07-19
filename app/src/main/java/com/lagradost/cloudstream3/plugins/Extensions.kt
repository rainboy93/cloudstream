package com.lagradost.cloudstream3.plugins

fun String?.removeNonNumeric(): String {
    return this?.replace(Regex("[^0-9]"), "")?.trim() ?: ""
}

fun String?.toInteger(): Int {
    return this.removeNonNumeric().toIntOrNull() ?: -1
}