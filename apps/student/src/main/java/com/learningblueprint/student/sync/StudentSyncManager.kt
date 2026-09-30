package com.learningblueprint.student.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import com.learningblueprint.core.model.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object StudentSyncManager {

    private const val PREFS_NAME = "student_blueprint_store"
    private const val KEY_CACHED_CONFIG = "cached_app_config"
    private const val KEY_REMOTE_URL = "custom_remote_sync_url"

    // Default public endpoint (configurable by Admin or Student)
    const val DEFAULT_REMOTE_URL = "https://raw.githubusercontent.com/learning-blueprint/content/main/announcements.json"

    fun getCachedConfig(context: Context): AppConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CACHED_CONFIG, null)
        return if (json != null) {
            AppConfig.fromJsonString(json) ?: AppConfig.DEFAULT
        } else {
            AppConfig.DEFAULT
        }
    }

    fun saveConfigLocally(context: Context, config: AppConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CACHED_CONFIG, config.toJsonString()).apply()
    }

    fun getRemoteSyncUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REMOTE_URL, DEFAULT_REMOTE_URL) ?: DEFAULT_REMOTE_URL
    }

    fun setRemoteSyncUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REMOTE_URL, url.trim()).apply()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Multi-Tier Sync Flow:
     * 1. Try Local IPC Provider (if Guru Ji Admin is on same phone)
     * 2. Try Remote Cloud HTTPS Endpoint (for cross-device synchronization)
     * 3. Fallback to Local Cached data if network fails
     */
    suspend fun syncAll(context: Context): Pair<AppConfig, String> = withContext(Dispatchers.IO) {
        // Tier 1: Check Local Admin Provider on same device
        try {
            val uri = Uri.parse("content://com.learningblueprint.admin.provider")
            val bundle = context.contentResolver.call(uri, "get_published_config", null, null)
            val jsonString = bundle?.getString("config_json")
            if (!jsonString.isNullOrBlank()) {
                val parsed = AppConfig.fromJsonString(jsonString)
                if (parsed != null) {
                    saveConfigLocally(context, parsed)
                    return@withContext Pair(parsed, "लोकल एडमिन ऐप से सिंक सफल")
                }
            }
        } catch (_: Exception) {
            // Local provider not found; continue to Remote Cloud Tier
        }

        // Tier 2: Check Remote Cloud URL across devices
        if (isNetworkAvailable(context)) {
            val remoteUrl = getRemoteSyncUrl(context)
            try {
                val url = URL(remoteUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4500
                    readTimeout = 4500
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "LearningBlueprint-StudentApp/4.0")
                }

                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()
                    conn.disconnect()

                    val parsed = AppConfig.fromJsonString(sb.toString())
                    if (parsed != null) {
                        saveConfigLocally(context, parsed)
                        return@withContext Pair(parsed, "क्लाउड से लाइव सिंक सफल")
                    }
                }
            } catch (e: Exception) {
                // Network error; will smoothly fall back to cached data
            }
        }

        // Tier 3: Local Cached Fallback
        val cached = getCachedConfig(context)
        Pair(cached, if (isNetworkAvailable(context)) "लोकल कैश्ड डेटा लोड हुआ" else "ऑफ़लाइन मोड (लोकल कैशे)")
    }
}
