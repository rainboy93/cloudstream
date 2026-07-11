package com.lagradost.cloudstream3.cloudsync

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.core.type.TypeReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lagradost.cloudstream3.cloudsync.CloudSyncManager.syncNow
import com.lagradost.cloudstream3.mapper
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.DataStore.getDefaultSharedPrefs
import com.lagradost.cloudstream3.utils.DataStore.getSyncMtimes
import com.lagradost.cloudstream3.utils.DataStore.putSyncMtimes
import java.util.UUID

/**
 * Cross-device sync of the transferable user data (continue-watching, playback
 * progress, bookmarks, favorites, settings) backed by Firebase Auth + Firestore.
 *
 * The synced payload is exactly [BackupUtils.getBackup] serialized to JSON and
 * stored at `users/{uid}`.
 *
 * Sync is event-driven rather than real-time:
 *  - **Pull** (download + merge) when the app enters the foreground, and right
 *    after sign-in.
 *  - **Push** (upload) when the app goes to the background, and when leaving the
 *    player screen (via [syncNow]).
 *
 * Conflict resolution: union of keys with per-key last-writer-wins. Every synced
 * data key carries a local modified-time (see [DataStore.getSyncMtimes]); on merge
 * the newer write of an overlapping key wins. Keys without a known timestamp on
 * either side fall back to remote-wins. Distinct items never clobber each other.
 */
object CloudSyncManager {
    private const val DEVICE_ID_KEY = "cloud_sync_device_id"
    private const val USERS_COLLECTION = "users"
    private const val FIELD_PAYLOAD = "payload"
    private const val FIELD_DEVICE = "deviceId"
    private const val FIELD_VERSION = "version"
    private const val FIELD_UPDATED_AT = "updatedAt"

    /** JSON string of `key -> lastModifiedMs` for per-key last-writer-wins. */
    private const val FIELD_TIMESTAMPS = "timestamps"

    sealed class SyncStatus {
        @Keep
        data object LoggedOut : SyncStatus()

        @Keep
        data object Syncing : SyncStatus()

        @Keep
        data class Synced(val timeMs: Long) : SyncStatus()

        @Keep
        data class Error(val message: String) : SyncStatus()
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var appContext: Context? = null
    private lateinit var deviceId: String

    private var startedActivities = 0
    private var needsInitialUpload = false
    private var lastUploadedVersion = 0L

    @Volatile
    private var lastUploadRequestMs = 0L

    /** Don't pull right after a push, so we never clobber just-saved local progress. */
    private const val PULL_COOLDOWN_AFTER_UPLOAD_MS = 4000L

    private val _status = MutableLiveData<SyncStatus>(SyncStatus.LoggedOut)
    val status: LiveData<SyncStatus> get() = _status

    /** Fires (with the current time) every time a remote change is applied locally. */
    val onRemoteApplied = MutableLiveData<Long>()

    /** Call once early (e.g. MainActivity.onCreate). Registers lifecycle triggers. */
    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        deviceId = context.getDefaultSharedPrefs().getString(DEVICE_ID_KEY, null)
            ?: UUID.randomUUID().toString().also {
                context.getDefaultSharedPrefs().edit().putString(DEVICE_ID_KEY, it).apply()
            }
        registerLifecycleCallbacks(context.applicationContext as? Application)
        if (auth.currentUser != null) _status.postValue(SyncStatus.Synced(System.currentTimeMillis()))
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun currentEmail(): String? = auth.currentUser?.email

    fun signIn(email: String, password: String, callback: (Result<Unit>) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onSignedIn(); callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun signUp(email: String, password: String, callback: (Result<Unit>) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onSignedIn(); callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun signOut() {
        auth.signOut()
        _status.postValue(SyncStatus.LoggedOut)
    }

    /** Push local data now. Called when leaving the player and from the manual button. */
    fun syncNow() {
        if (!isLoggedIn()) return
        uploadLocal()
    }

    /**
     * Pull remote data now, e.g. when opening a title's detail screen. Skipped for a
     * few seconds after a push so it can't overwrite progress we just uploaded.
     */
    fun pull() {
        if (!isLoggedIn()) return
        if (System.currentTimeMillis() - lastUploadRequestMs < PULL_COOLDOWN_AFTER_UPLOAD_MS) return
        downloadRemote()
    }

    private fun onSignedIn() {
        // Reconcile with the cloud: pull remote, then push the merged union back so
        // any local-only data reaches other devices.
        needsInitialUpload = true
        downloadRemote()
    }

    private fun registerLifecycleCallbacks(application: Application?) {
        application ?: return
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (startedActivities == 0) onAppForeground()
                startedActivities++
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) onAppBackground()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun onAppForeground() {
        if (isLoggedIn()) downloadRemote()
    }

    private fun onAppBackground() {
        if (isLoggedIn()) uploadLocal()
    }

    private fun downloadRemote() {
        val uid = auth.currentUser?.uid ?: return
        _status.postValue(SyncStatus.Syncing)
        firestore.collection(USERS_COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val payload = if (snapshot != null && snapshot.exists()) {
                    snapshot.getString(FIELD_PAYLOAD)
                } else null

                // Whether to push the merged union back after reconciling (set on sign-in).
                val uploadAfter = needsInitialUpload
                needsInitialUpload = false

                if (payload != null) {
                    val version = snapshot!!.getLong(FIELD_VERSION) ?: 0L
                    val device = snapshot.getString(FIELD_DEVICE)
                    // Skip re-applying our own latest upload.
                    if (!(device == deviceId && version == lastUploadedVersion)) {
                        val remoteTimestamps = parseTimestamps(snapshot.getString(FIELD_TIMESTAMPS))
                        // Upload only after the merge+restore finishes, so we push the merged
                        // union rather than clobbering the cloud with pre-merge local data.
                        ioSafe {
                            applyRemote(payload, remoteTimestamps)
                            if (uploadAfter) uploadLocal()
                        }
                    } else {
                        _status.postValue(SyncStatus.Synced(System.currentTimeMillis()))
                        if (uploadAfter) uploadLocal()
                    }
                } else {
                    _status.postValue(SyncStatus.Synced(System.currentTimeMillis()))
                    if (uploadAfter) uploadLocal()
                }
            }
            .addOnFailureListener {
                _status.postValue(SyncStatus.Error(it.message ?: "download failed"))
                if (needsInitialUpload) {
                    needsInitialUpload = false
                    uploadLocal()
                }
            }
    }

    private fun applyRemote(payload: String, remoteTimestamps: Map<String, Long>) {
        val context = appContext ?: return
        try {
            val remote = mapper.readValue(payload, BackupUtils.BackupFile::class.java)
            val local = BackupUtils.getBackup(context)
            val localTimestamps = context.getSyncMtimes()

            val (merged, mergedTimestamps) = if (local == null) {
                remote to remoteTimestamps
            } else {
                mergeFile(local, localTimestamps, remote, remoteTimestamps)
            }

            BackupUtils.restore(
                context,
                merged,
                restoreSettings = true,
                restoreDataStore = true
            )

            // Restore uses raw writes that bypass mtime stamping, so record the winning
            // timestamps explicitly. Later uploads then carry accurate recency to peers.
            context.putSyncMtimes(mergedTimestamps)

            val now = System.currentTimeMillis()
            _status.postValue(SyncStatus.Synced(now))
            main { onRemoteApplied.value = now }
        } catch (e: Exception) {
            logError(e)
            _status.postValue(SyncStatus.Error(e.message ?: "merge failed"))
        }
    }

    private fun parseTimestamps(json: String?): Map<String, Long> {
        if (json.isNullOrEmpty()) return emptyMap()
        return try {
            mapper.readValue(json, object : TypeReference<Map<String, Long>>() {})
        } catch (e: Exception) {
            logError(e)
            emptyMap()
        }
    }

    private fun uploadLocal() {
        val context = appContext ?: return
        val uid = auth.currentUser?.uid ?: return
        lastUploadRequestMs = System.currentTimeMillis()
        try {
            val file = BackupUtils.getBackup(context) ?: return
            val json = mapper.writeValueAsString(file)
            val timestampsJson = mapper.writeValueAsString(context.getSyncMtimes())
            val version = System.currentTimeMillis()
            val document = mapOf(
                FIELD_DEVICE to deviceId,
                FIELD_VERSION to version,
                FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                FIELD_PAYLOAD to json,
                FIELD_TIMESTAMPS to timestampsJson,
            )
            _status.postValue(SyncStatus.Syncing)
            firestore.collection(USERS_COLLECTION).document(uid).set(document)
                .addOnSuccessListener {
                    lastUploadedVersion = version
                    _status.postValue(SyncStatus.Synced(System.currentTimeMillis()))
                }
                .addOnFailureListener {
                    _status.postValue(SyncStatus.Error(it.message ?: "upload failed"))
                }
        } catch (e: Exception) {
            logError(e)
            _status.postValue(SyncStatus.Error(e.message ?: "upload failed"))
        }
    }

    /**
     * Union of keys with per-key last-writer-wins. On an overlapping key the side with
     * the newer timestamp wins; ties and keys unknown to both sides fall back to
     * remote-wins. Returns the merged file plus the merged per-key timestamps.
     */
    private fun mergeFile(
        local: BackupUtils.BackupFile,
        localTs: Map<String, Long>,
        remote: BackupUtils.BackupFile,
        remoteTs: Map<String, Long>,
    ): Pair<BackupUtils.BackupFile, Map<String, Long>> {
        val merged = BackupUtils.BackupFile(
            datastore = mergeVars(local.datastore, remote.datastore, localTs, remoteTs),
            settings = mergeVars(local.settings, remote.settings, localTs, remoteTs),
        )
        return merged to computeMergedTimestamps(merged, localTs, remoteTs)
    }

    private fun mergeVars(
        local: BackupUtils.BackupVars,
        remote: BackupUtils.BackupVars,
        localTs: Map<String, Long>,
        remoteTs: Map<String, Long>,
    ): BackupUtils.BackupVars {
        fun <V> merge(a: Map<String, V>?, b: Map<String, V>?): Map<String, V>? {
            if (a == null) return b
            if (b == null) return a
            val out = LinkedHashMap(a)
            for ((key, value) in b) {
                val remoteWins = (remoteTs[key] ?: 0L) >= (localTs[key] ?: 0L)
                if (!out.containsKey(key) || remoteWins) out[key] = value
            }
            return out
        }
        return BackupUtils.BackupVars(
            bool = merge(local.bool, remote.bool),
            int = merge(local.int, remote.int),
            string = merge(local.string, remote.string),
            float = merge(local.float, remote.float),
            long = merge(local.long, remote.long),
            stringSet = merge(local.stringSet, remote.stringSet),
        )
    }

    /** For every key that survived the merge, keep the newer of the two timestamps. */
    private fun computeMergedTimestamps(
        merged: BackupUtils.BackupFile,
        localTs: Map<String, Long>,
        remoteTs: Map<String, Long>,
    ): Map<String, Long> {
        val keys = HashSet<String>()
        fun collect(vars: BackupUtils.BackupVars) {
            vars.bool?.keys?.let { keys += it }
            vars.int?.keys?.let { keys += it }
            vars.string?.keys?.let { keys += it }
            vars.float?.keys?.let { keys += it }
            vars.long?.keys?.let { keys += it }
            vars.stringSet?.keys?.let { keys += it }
        }
        collect(merged.datastore)
        collect(merged.settings)

        val out = HashMap<String, Long>(keys.size)
        for (key in keys) {
            val time = maxOf(localTs[key] ?: 0L, remoteTs[key] ?: 0L)
            if (time > 0L) out[key] = time
        }
        return out
    }
}
