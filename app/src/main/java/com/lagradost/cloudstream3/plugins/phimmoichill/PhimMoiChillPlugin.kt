package com.lagradost.cloudstream3.plugins.phimmoichill

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class PhimMoiChillPlugin : Plugin() {
    var activity: AppCompatActivity? = null

    override fun load(context: Context) {
        activity = context as AppCompatActivity
        registerMainAPI(PhimMoiChillProvider(this))
    }
}
