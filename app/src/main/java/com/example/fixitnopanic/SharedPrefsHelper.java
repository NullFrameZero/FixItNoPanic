package com.example.fixitnopanic;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private final SharedPreferences prefs;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
    }

    public void setCurrentScreen(String screen) {
        prefs.edit().putString("current_screen", screen).apply();
    }

    public String getCurrentScreen() {
        return prefs.getString("current_screen", "main");
    }
}