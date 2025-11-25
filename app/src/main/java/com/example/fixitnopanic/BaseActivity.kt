package com.example.fixitnopanic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefsHelper = SharedPrefsHelper(this)
    }

    override fun onBackPressed() {
        sharedPrefsHelper.setCurrentScreen("main")
        super.onBackPressed()
    }
}