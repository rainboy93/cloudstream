package com.lagradost.cloudstream3.plugins.bluphim

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class Phim1080Plugin : Plugin() {
    override val provider: MainAPI by lazy { Phim1080Provider(this) }
}