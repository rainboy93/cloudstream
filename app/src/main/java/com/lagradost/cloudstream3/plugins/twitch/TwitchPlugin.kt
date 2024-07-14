package com.lagradost.cloudstream3.plugins.twitch

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class TwitchPlugin : Plugin() {
    override val provider: MainAPI by lazy { TwitchProvider() }
}