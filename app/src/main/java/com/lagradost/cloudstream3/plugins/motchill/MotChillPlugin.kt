package com.lagradost.cloudstream3.plugins.motchill

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class MotChillPlugin : Plugin() {
    override val provider: MainAPI by lazy { MotChillProvider(this) }
}