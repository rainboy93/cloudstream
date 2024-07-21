package com.lagradost.cloudstream3.plugins.ophim

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class OPhimPlugin : Plugin() {
    override val provider: MainAPI by lazy { OPhimProvider() }
}