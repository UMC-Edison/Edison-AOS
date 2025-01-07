package com.umc.edison.remote.token

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private var ACCESS_TOKEN: String? = null
    }

    fun loadAccessToken(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE)
        ACCESS_TOKEN = sharedPreferences.getString("access_token", null)

        return ACCESS_TOKEN
    }

    fun setToken(token: String) {
        ACCESS_TOKEN = token
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("access_token", token)
    }
}