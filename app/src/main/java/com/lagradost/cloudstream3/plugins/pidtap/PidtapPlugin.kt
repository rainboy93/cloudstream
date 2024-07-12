package com.lagradost.cloudstream3.plugins.pidtap

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class PidtapPlugin : Plugin() {
    override val provider: MainAPI by lazy { PidtapProvider(this) }
}