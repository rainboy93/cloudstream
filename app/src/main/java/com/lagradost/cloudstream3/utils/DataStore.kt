package com.lagradost.cloudstream3.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKeyClass
import com.lagradost.cloudstream3.CloudStreamApp.Companion.removeKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKeyClass
import com.lagradost.cloudstream3.mvvm.logError
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/** Used to display metadata about downloads and resume watching */
const val DOWNLOAD_HEADER_CACHE = "download_header_cache"
const val DOWNLOAD_HEADER_CACHE_BACKUP = "BACKUP_download_header_cache"

//const val WATCH_HEADER_CACHE = "watch_header_cache"
const val DOWNLOAD_EPISODE_CACHE = "download_episode_cache"
const val DOWNLOAD_EPISODE_CACHE_BACKUP = "BACKUP_download_episode_cache"
const val VIDEO_PLAYER_BRIGHTNESS = "video_player_alpha_key"
const val USER_SELECTED_HOMEPAGE_API = "home_api_used"
const val USER_PROVIDER_API = "user_custom_sites"
const val PREFERENCES_NAME = "rebuild_preference"

/**
 * Sidecar store mapping each data key to the epoch-ms it was last modified locally.
 * Kept in its own prefs file so it is never itself backed up or synced, and used by
 * cloud sync to resolve conflicts with per-key last-writer-wins.
 */
const val SYNC_MTIME_PREFERENCES_NAME = "cloudsync_mtime"

// TODO degelgate by value for get & set

class PreferenceDelegate<T : Any>(
    val key: String, val default: T //, private val klass: KClass<T>
) {
    private val klass: KClass<out T> = default::class

    // simple cache to make it not get the key every time it is accessed, however this requires
    // that ONLY this changes the key
    private var cache: T? = null

    operator fun getValue(self: Any?, property: KProperty<*>) =
        cache ?: getKeyClass(key, klass.java).also { newCache -> cache = newCache } ?: default

    operator fun setValue(
        self: Any?,
        property: KProperty<*>,
        t: T?
    ) {
        cache = t
        if (t == null) {
            removeKey(key)
        } else {
            setKeyClass(key, t)
        }
    }
}

/** When inserting many keys use this function, this is because apply for every key is very expensive on memory */
data class Editor(
    val editor: SharedPreferences.Editor
) {
    /** Always remember to call apply after */
    fun <T> setKeyRaw(path: String, value: T) {
        @Suppress("UNCHECKED_CAST")
        if (isStringSet(value)) {
            editor.putStringSet(path, value as Set<String>)
        } else {
            when (value) {
                is Boolean -> editor.putBoolean(path, value)
                is Int -> editor.putInt(path, value)
                is String -> editor.putString(path, value)
                is Float -> editor.putFloat(path, value)
                is Long -> editor.putLong(path, value)
            }
        }
    }

    private fun isStringSet(value: Any?): Boolean {
        if (value is Set<*>) {
            return value.filterIsInstance<String>().size == value.size
        }
        return false
    }

    fun apply() {
        editor.apply()
        System.gc()
    }
}

object DataStore {
    val mapper: JsonMapper = JsonMapper.builder().addModule(kotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun Context.getSharedPrefs(): SharedPreferences {
        return getPreferences(this)
    }


    fun getFolderName(folder: String, path: String): String {
        return "${folder}/${path}"
    }

    fun editor(context: Context, isEditingAppSettings: Boolean = false): Editor {
        val editor: SharedPreferences.Editor =
            if (isEditingAppSettings) context.getDefaultSharedPrefs()
                .edit() else context.getSharedPrefs().edit()
        return Editor(editor)
    }

    fun Context.getDefaultSharedPrefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun Context.getKeys(folder: String): List<String> {
        // Ensure that the folder ends with "/" to prevent matching with other folders
        val fixedFolder = folder.trimEnd('/') + "/"
        return this.getSharedPrefs().all.keys.filter { it.startsWith(fixedFolder) }
    }

    fun Context.removeKey(folder: String, path: String) {
        removeKey(getFolderName(folder, path))
    }

    fun Context.containsKey(folder: String, path: String): Boolean {
        return containsKey(getFolderName(folder, path))
    }

    fun Context.containsKey(path: String): Boolean {
        val prefs = getSharedPrefs()
        return prefs.contains(path)
    }

    private fun Context.getSyncMtimePrefs(): SharedPreferences {
        return getSharedPreferences(SYNC_MTIME_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    /** Stamp [path] as modified now, so cloud sync can tell which copy is newer. */
    fun Context.recordSyncMtime(path: String, time: Long = System.currentTimeMillis()) {
        try {
            getSyncMtimePrefs().edit { putLong(path, time) }
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun Context.recordSyncMtimes(
        paths: Collection<String>,
        time: Long = System.currentTimeMillis(),
    ) {
        if (paths.isEmpty()) return
        try {
            getSyncMtimePrefs().edit {
                paths.forEach { putLong(it, time) }
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    private fun Context.removeSyncMtimes(paths: Collection<String>) {
        if (paths.isEmpty()) return
        try {
            getSyncMtimePrefs().edit {
                paths.forEach { remove(it) }
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun Context.getSyncMtimes(): Map<String, Long> {
        return try {
            getSyncMtimePrefs().all.filterValues { it is Long } as Map<String, Long>
        } catch (e: Exception) {
            logError(e)
            emptyMap()
        }
    }

    /** Upsert (never clears) so concurrent local writes are not lost. */
    fun Context.putSyncMtimes(times: Map<String, Long>) {
        if (times.isEmpty()) return
        try {
            getSyncMtimePrefs().edit {
                times.forEach { (key, value) -> putLong(key, value) }
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun Context.removeKey(path: String) {
        try {
            val prefs = getSharedPrefs()
            if (prefs.contains(path)) {
                prefs.edit {
                    remove(path)
                }
            }
            removeSyncMtimes(listOf(path))
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun Context.removeKeys(folder: String): Int {
        val keys = getKeys("$folder/")
        try {
            getSharedPrefs().edit {
                keys.forEach { value ->
                    remove(value)
                }
            }
            removeSyncMtimes(keys)
            return keys.size
        } catch (e: Exception) {
            logError(e)
            return 0
        }
    }

    fun <T> Context.setKey(path: String, value: T) {
        try {
            getSharedPrefs().edit {
                putString(path, mapper.writeValueAsString(value))
            }
            recordSyncMtime(path)
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun <T> Context.getKey(path: String, valueType: Class<T>): T? {
        try {
            val json: String = getSharedPrefs().getString(path, null) ?: return null
            return json.toKotlinObject(valueType)
        } catch (e: Exception) {
            return null
        }
    }

    fun <T> Context.setKey(folder: String, path: String, value: T) {
        setKey(getFolderName(folder, path), value)
    }

    inline fun <reified T : Any> String.toKotlinObject(): T {
        return mapper.readValue(this, T::class.java)
    }

    fun <T> String.toKotlinObject(valueType: Class<T>): T {
        return mapper.readValue(this, valueType)
    }

    // GET KEY GIVEN PATH AND DEFAULT VALUE, NULL IF ERROR
    inline fun <reified T : Any> Context.getKey(path: String, defVal: T?): T? {
        try {
            val json: String = getSharedPrefs().getString(path, null) ?: return defVal
            return json.toKotlinObject()
        } catch (e: Exception) {
            return null
        }
    }

    inline fun <reified T : Any> Context.getKey(path: String): T? {
        return getKey(path, null)
    }

    inline fun <reified T : Any> Context.getKey(folder: String, path: String): T? {
        return getKey(getFolderName(folder, path), null)
    }

    inline fun <reified T : Any> Context.getKey(folder: String, path: String, defVal: T?): T? {
        return getKey(getFolderName(folder, path), defVal) ?: defVal
    }
}