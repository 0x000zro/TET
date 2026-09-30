package com.learningblueprint.admin.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

class AdminConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val prefs = context?.getSharedPreferences("admin_sync_store", Context.MODE_PRIVATE) ?: return null
        val response = Bundle()

        when (method) {
            "get_published_config" -> {
                val json = prefs.getString("published_config", null)
                response.putString("config_json", json)
                return response
            }
            "publish_config" -> {
                val json = extras?.getString("config_json")
                if (json != null) {
                    prefs.edit().putString("published_config", json).apply()
                    response.putBoolean("success", true)
                }
                return response
            }
            else -> return null
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
