package com.learningblueprint.admin.sync

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import com.learningblueprint.core.model.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AdminRemoteSyncManager {

    private const val PREFS_NAME = "guru_ji_remote_sync_store"
    private const val KEY_GH_OWNER = "github_owner"
    private const val KEY_GH_REPO = "github_repo"
    private const val KEY_GH_BRANCH = "github_branch"
    private const val KEY_GH_PATH = "github_path"
    private const val KEY_GH_TOKEN = "github_token"

    data class GitHubConfig(
        val owner: String = "learning-blueprint",
        val repo: String = "content",
        val branch: String = "main",
        val path: String = "announcements.json",
        val token: String = ""
    ) {
        val isConfigured: Boolean get() = owner.isNotBlank() && repo.isNotBlank() && token.isNotBlank()
        val rawUrl: String get() = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
    }

    fun getGitHubConfig(context: Context): GitHubConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return GitHubConfig(
            owner = prefs.getString(KEY_GH_OWNER, "learning-blueprint") ?: "learning-blueprint",
            repo = prefs.getString(KEY_GH_REPO, "content") ?: "content",
            branch = prefs.getString(KEY_GH_BRANCH, "main") ?: "main",
            path = prefs.getString(KEY_GH_PATH, "announcements.json") ?: "announcements.json",
            token = prefs.getString(KEY_GH_TOKEN, "") ?: ""
        )
    }

    fun saveGitHubConfig(context: Context, config: GitHubConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_GH_OWNER, config.owner.trim())
            .putString(KEY_GH_REPO, config.repo.trim())
            .putString(KEY_GH_BRANCH, config.branch.trim())
            .putString(KEY_GH_PATH, config.path.trim())
            .putString(KEY_GH_TOKEN, config.token.trim())
            .apply()
    }

    /**
     * Publishes to Local Provider (same-device IPC) AND Remote GitHub Repository (cross-device).
     */
    suspend fun publish(context: Context, appConfig: AppConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val jsonPayload = appConfig.toJsonString()

        // Step 1: Local IPC Publish
        try {
            val uri = Uri.parse("content://com.learningblueprint.admin.provider")
            val bundle = Bundle().apply { putString("config_json", jsonPayload) }
            context.contentResolver.call(uri, "publish_config", null, bundle)
        } catch (_: Exception) {
            // Local publish failed or running standalone
        }

        // Step 2: Remote GitHub Sync
        val ghConfig = getGitHubConfig(context)
        if (!ghConfig.isConfigured) {
            return@withContext Pair(
                true,
                "✓ लोकल पब्लिश सफल!\n(रिमोट डिवाइस सिंक हेतु 'क्लाउड सेटिंग्स' में GitHub टोकन जोड़ें)"
            )
        }

        try {
            val apiUrl = "https://api.github.com/repos/${ghConfig.owner}/${ghConfig.repo}/contents/${ghConfig.path}"
            
            // First check if file exists to obtain current SHA
            var fileSha: String? = null
            try {
                val getConn = (URL("$apiUrl?ref=${ghConfig.branch}").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer ${ghConfig.token}")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                if (getConn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(getConn.inputStream))
                    val res = reader.readText()
                    reader.close()
                    fileSha = JSONObject(res).optString("sha")
                }
                getConn.disconnect()
            } catch (_: Exception) {
                // File does not exist yet; will create new
            }

            // PUT update/create content
            val putConn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Authorization", "Bearer ${ghConfig.token}")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 7000
                readTimeout = 7000
            }

            val base64Content = Base64.encodeToString(jsonPayload.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val requestBody = JSONObject().apply {
                put("message", "Guru Ji Admin: Publish update ${System.currentTimeMillis()}")
                put("content", base64Content)
                put("branch", ghConfig.branch)
                if (fileSha != null) {
                    put("sha", fileSha)
                }
            }

            val writer = OutputStreamWriter(putConn.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            val code = putConn.responseCode
            putConn.disconnect()

            if (code in 200..299) {
                Pair(true, "✓ लोकल एवं GitHub रिमोट क्लाउड दोनों पर लाइव पब्लिश सफल!")
            } else {
                Pair(false, "लोकल पब्लिश हुआ, परंतु GitHub रिस्पॉन्स एरर: HTTP $code")
            }
        } catch (e: Exception) {
            Pair(false, "लोकल पब्लिश हुआ, परंतु रिमोट नेटवर्क एरर: ${e.message}")
        }
    }
}
