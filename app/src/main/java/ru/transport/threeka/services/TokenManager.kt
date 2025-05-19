package ru.transport.threeka.services

import android.content.Context
import ru.transport.threeka.api.schemas.Token

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: Token) {
        prefs.edit()
            .putString("access_token", token.access_token)
            .putString("refresh_token", token.refresh_token)
            .putLong("expires_in", token.expires_in)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun getExpiresIn(): Long = prefs.getLong("expires_in", 0L)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}