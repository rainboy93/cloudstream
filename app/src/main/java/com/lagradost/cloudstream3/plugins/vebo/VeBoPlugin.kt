package com.lagradost.cloudstream3.plugins.vebo

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class VeBoPlugin : Plugin() {
    override val provider: MainAPI by lazy { VeBoProvider(this) }
}