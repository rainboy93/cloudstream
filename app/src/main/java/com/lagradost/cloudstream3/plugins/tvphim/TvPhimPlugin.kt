package com.lagradost.cloudstream3.plugins.tvphim

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class TvPhimPlugin : Plugin() {
    override val provider: MainAPI by lazy { TvPhimProvider() }
}