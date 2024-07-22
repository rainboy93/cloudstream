package com.lagradost.cloudstream3.plugins.blog

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.plugins.Plugin

class BlogTruyenPlugin : Plugin() {
    override val provider: MainAPI by lazy { BlogTruyenProvider() }
}