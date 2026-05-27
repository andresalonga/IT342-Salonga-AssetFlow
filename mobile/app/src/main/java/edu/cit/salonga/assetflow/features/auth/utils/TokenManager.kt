package edu.cit.salonga.assetflow.features.auth.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val TAG = "TokenManager"
    private const val PREF_NAME = "AssetFlow_Prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        this.context = context.applicationContext
        try {
            initSharedPreferences(this.context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, wiping file...", e)
            try {
                this.context.deleteSharedPreferences(PREF_NAME)
                initSharedPreferences(this.context)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to recreate EncryptedSharedPreferences, falling back to standard SharedPreferences", ex)
                sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private fun initSharedPreferences(ctx: Context) {
        val masterKey = MasterKey.Builder(ctx, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            ctx,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        try {
            sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token, retrying after wipe...", e)
            try {
                context.deleteSharedPreferences(PREF_NAME)
                init(context)
                sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to save token after wipe fallback", ex)
            }
        }
    }

    fun getToken(): String? {
        return try {
            sharedPreferences.getString(KEY_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading token", e)
            null
        }
    }

    fun saveUserInfo(userId: Long, name: String, email: String, role: String) {
        try {
            sharedPreferences.edit().apply {
                putLong(KEY_USER_ID, userId)
                putString(KEY_USER_NAME, name)
                putString(KEY_USER_EMAIL, email)
                putString(KEY_USER_ROLE, role)
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user info, retrying after wipe...", e)
            try {
                context.deleteSharedPreferences(PREF_NAME)
                init(context)
                sharedPreferences.edit().apply {
                    putLong(KEY_USER_ID, userId)
                    putString(KEY_USER_NAME, name)
                    putString(KEY_USER_EMAIL, email)
                    putString(KEY_USER_ROLE, role)
                    apply()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to save user info after wipe fallback", ex)
            }
        }
    }

    fun getUserId(): Long {
        return try {
            sharedPreferences.getLong(KEY_USER_ID, -1)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading user ID", e)
            -1
        }
    }

    fun getUserName(): String? {
        return try {
            sharedPreferences.getString(KEY_USER_NAME, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading user name", e)
            null
        }
    }

    fun getUserEmail(): String? {
        return try {
            sharedPreferences.getString(KEY_USER_EMAIL, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading user email", e)
            null
        }
    }

    fun getUserRole(): String? {
        return try {
            sharedPreferences.getString(KEY_USER_ROLE, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading user role", e)
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearAll() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing shared preferences, deleting file manually...", e)
            try {
                context.deleteSharedPreferences(PREF_NAME)
                init(context)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to delete and re-initialize shared preferences during clearAll", ex)
            }
        }
    }
}
