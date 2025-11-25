package com.example.fixitnopanic

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun setCurrentScreen(screen: String) {
        prefs.edit().putString("current_screen", screen).apply()
    }

    fun getCurrentScreen(): String {
        return prefs.getString("current_screen", "main") ?: "main"
    }
}