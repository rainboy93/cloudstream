package com.lagradost.cloudstream3.plugins.nguonc

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class NguonCPlugin : Plugin() {
    override val provider: MainAPI by lazy { NguonCProvider() }
}