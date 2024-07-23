package com.lagradost.cloudstream3.plugins.tvphim

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageData
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.mainPageOf

class TvPhimProvider : MainAPI() {

    override var mainUrl = "https://tvphim.us"
    override var name = "TV Phim"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "vi"

    override val hasMainPage = true

    override val mainPage: List<MainPageData> = mainPageOf(
        "$mainUrl/phim-le/page/" to "Phim Lẻ",
        "$mainUrl/phim-bo/page/" to "Phim Bộ",
        "$mainUrl/quoc-gia/phim-han-quoc/page/" to "Phim Hàn Quốc",
        "$mainUrl/quoc-gia/phim-trung-quoc/page/" to "Phim Trung Quốc",
        "$mainUrl/quoc-gia/phim-my/page/" to "Phim Mỹ"
    )
}