package com.lagradost.cloudstream3.plugins.tmdb

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class TMDBPlugin : Plugin() {
    override val provider: MainAPI by lazy { TMDBProvider() }
}