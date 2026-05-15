package com.android.rumahsehatmannawasalwa.data.local.pref

import android.content.Context
import android.util.Log
import com.android.rumahsehatmannawasalwa.data.model.auth.User
import com.google.gson.Gson

class UserPreference(context: Context) {
    
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        val json = gson.toJson(user)
        preferences.edit().putString(KEY_USER, json).apply()
        // Also save token separately for easy access if needed, though it's in User
        user.accessToken?.let { saveToken(it) }
    }

    fun getUser(): User? {
        val json = preferences.getString(KEY_USER, null)
        Log.d("TRACKER_PREF", "Membaca Cache User: ${if (json != null) "Ditemukan" else "KOSONG"}")
        return if (json != null) {
            try {
                val user = gson.fromJson(json, User::class.java)
                Log.d("TRACKER_PREF", "Role: ${user.role}, SanctumToken: ${user.accessToken?.take(10)}...")
                user
            } catch (e: Exception) {
                Log.e("TRACKER_PREF", "Error Parsing JSON: ${e.message}")
                null
            }
        } else null
    }
    
    fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun logout() {
        preferences.edit().clear().apply()
    }

    fun setNotificationEnabled(isEnabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIF, isEnabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return preferences.getBoolean(KEY_NOTIF, true) // Default true
    }

    fun saveFcmToken(token: String) {
        preferences.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String? {
        return preferences.getString(KEY_FCM_TOKEN, null)
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USER = "user_obj"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_NOTIF = "notification_enabled"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
