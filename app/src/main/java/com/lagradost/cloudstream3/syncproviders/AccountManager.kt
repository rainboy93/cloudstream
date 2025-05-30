package com.lagradost.cloudstream3.syncproviders

import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.removeKeys
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
import com.lagradost.cloudstream3.syncproviders.providers.Addic7ed
import com.lagradost.cloudstream3.syncproviders.providers.AniListApi
import com.lagradost.cloudstream3.syncproviders.providers.IndexSubtitleApi
import com.lagradost.cloudstream3.syncproviders.providers.LocalList
import com.lagradost.cloudstream3.syncproviders.providers.MALApi
import com.lagradost.cloudstream3.syncproviders.providers.OpenSubtitlesApi
import com.lagradost.cloudstream3.syncproviders.providers.SimklApi
import com.lagradost.cloudstream3.syncproviders.providers.SubDLAPI
import com.lagradost.cloudstream3.syncproviders.providers.SubScene
import java.util.concurrent.TimeUnit

abstract class AccountManager(private val defIndex: Int) : AuthAPI {
    companion object {
        val malApi = MALApi(0)
        val aniListApi = AniListApi(0)
        val openSubtitlesApi = OpenSubtitlesApi(0)
        val simklApi = SimklApi(0)
        val indexSubtitlesApi = IndexSubtitleApi()
        val addic7ed = Addic7ed()
        val subScene = SubScene()
        val localListApi = LocalList()
        val subDlApi = SubDLAPI()

        // used to login via app intent
        val OAuth2Apis
            get() = listOf<OAuth2API>(
                malApi, aniListApi, simklApi
            )

        // this needs init with context and can be accessed in settings
        val accountManagers
            get() = listOf(
                malApi, aniListApi, openSubtitlesApi, simklApi //nginxApi
            )

        // used for active syncing
        val SyncApis
            get() = listOf(
                SyncRepo(malApi), SyncRepo(aniListApi), SyncRepo(localListApi), SyncRepo(simklApi)
            )

        val inAppAuths
            get() = listOf(openSubtitlesApi)//, nginxApi)

        val subtitleProviders
            get() = listOf(
//                openSubtitlesApi,
//                indexSubtitlesApi, // they got anti scraping measures in place :(
//                addic7ed,
//                subScene,
                subDlApi
            )

        const val appString = "cloudstreamapp"
        const val appStringRepo = "cloudstreamrepo"
        const val appStringPlayer = "cloudstreamplayer"

        // Instantly start the search given a query
        const val appStringSearch = "cloudstreamsearch"

        // Instantly resume watching a show
        const val appStringResumeWatching = "cloudstreamcontinuewatching"

        val unixTime: Long
            get() = System.currentTimeMillis() / 1000L
        val unixTimeMs: Long
            get() = System.currentTimeMillis()

        const val maxStale = 60 * 10

        fun secondsToReadable(seconds: Int, completedValue: String): String {
            var secondsLong = seconds.toLong()
            val days = TimeUnit.SECONDS
                .toDays(secondsLong)
            secondsLong -= TimeUnit.DAYS.toSeconds(days)

            val hours = TimeUnit.SECONDS
                .toHours(secondsLong)
            secondsLong -= TimeUnit.HOURS.toSeconds(hours)

            val minutes = TimeUnit.SECONDS
                .toMinutes(secondsLong)
            secondsLong -= TimeUnit.MINUTES.toSeconds(minutes)
            if (minutes < 0) {
                return completedValue
            }
            //println("$days $hours $minutes")
            return "${if (days != 0L) "$days" + "d " else ""}${if (hours != 0L) "$hours" + "h " else ""}${minutes}m"
        }
    }

    var accountIndex = defIndex
    private var lastAccountIndex = defIndex
    protected val accountId get() = "${idPrefix}_account_$accountIndex"
    private val accountActiveKey get() = "${idPrefix}_active"

    // int array of all accounts indexes
    private val accountsKey get() = "${idPrefix}_accounts"

    protected fun removeAccountKeys() {
        removeKeys(accountId)
        val accounts = getAccounts()?.toMutableList() ?: mutableListOf()
        accounts.remove(accountIndex)
        setKey(accountsKey, accounts.toIntArray())

        init()
    }

    fun getAccounts(): IntArray? {
        return getKey(accountsKey, intArrayOf())
    }

    fun init() {
        accountIndex = getKey(accountActiveKey, defIndex)!!
        val accounts = getAccounts()
        if (accounts?.isNotEmpty() == true && this.loginInfo() == null) {
            accountIndex = accounts.first()
        }
    }

    protected fun switchToNewAccount() {
        val accounts = getAccounts()
        lastAccountIndex = accountIndex
        accountIndex = (accounts?.maxOrNull() ?: 0) + 1
    }
    protected fun switchToOldAccount() {
        accountIndex = lastAccountIndex
    }

    protected fun registerAccount() {
        setKey(accountActiveKey, accountIndex)
        val accounts = getAccounts()?.toMutableList() ?: mutableListOf()
        if (!accounts.contains(accountIndex)) {
            accounts.add(accountIndex)
        }

        setKey(accountsKey, accounts.toIntArray())
    }

    fun changeAccount(index: Int) {
        accountIndex = index
        setKey(accountActiveKey, index)
    }
}
