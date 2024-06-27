package com.lagradost.cloudstream3.plugins.phimmoichill

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class PhimmoichillProviderPlugin : Plugin() {
    override val provider: MainAPI by lazy { PhimmoichillProvider() }
}