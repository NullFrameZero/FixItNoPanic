package com.example.fixitnopanic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var requestDao: RequestDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private lateinit var timeTextView: TextView
    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var buttonMain: Button
    private lateinit var buttonCreate: Button
    private lateinit var buttonStats: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestDao = RequestDao(this)
        sharedPrefsHelper = SharedPrefsHelper(this)
        timeTextView = findViewById(R.id.timeTextView)
        recyclerView = findViewById(R.id.recyclerViewRequests)
        buttonMain = findViewById(R.id.buttonMain)
        buttonCreate = findViewById(R.id.buttonCreate)
        buttonStats = findViewById(R.id.buttonStats)

        buttonMain.isEnabled = false
        setButtonSelected(buttonMain)

        updateTime()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val initialRequests = requestDao.getAllRequests()
        adapter = RequestAdapter(initialRequests, this, requestDao)
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(this)
        recyclerView.addItemDecoration(divider)

        sharedPrefsHelper.setCurrentScreen("main")

        buttonMain.setOnClickListener { setButtonSelected(buttonMain) }
        buttonCreate.setOnClickListener {
            setButtonSelected(buttonCreate)
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }
        buttonStats.setOnClickListener {
            setButtonSelected(buttonStats)
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        loadRequests()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (sharedPrefsHelper.getCurrentScreen() != "main") {
                    sharedPrefsHelper.setCurrentScreen("main")
                    updateButtonSelection()
                } else {
                    isEnabled = false
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateTime()
        loadRequests()
        updateButtonSelection()
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("EEE dd MMM yyyy, HH:mm", Locale.getDefault())
        timeTextView.text = dateFormat.format(currentTime)
    }

    private fun loadRequests() {
        adapter.updateData(requestDao.getAllRequests())
    }

    private fun setButtonSelected(selectedButton: Button) {
        resetButtonStyle(buttonMain)
        resetButtonStyle(buttonCreate)
        resetButtonStyle(buttonStats)

        selectedButton.setBackgroundResource(R.drawable.btn_nav_selected)
        selectedButton.setTextColor(Color.WHITE)
        selectedButton.isEnabled = (selectedButton.id != R.id.buttonMain)

        sharedPrefsHelper.setCurrentScreen(
            when (selectedButton.id) {
                R.id.buttonCreate -> "create"
                R.id.buttonStats -> "stats"
                else -> "main"
            }
        )
    }

    private fun resetButtonStyle(button: Button) {
        button.setBackgroundResource(R.drawable.btn_nav_selector)
        button.setTextColor(Color.parseColor("#4CAF50"))
        button.isEnabled = true
    }

    private fun updateButtonSelection() {
        when (sharedPrefsHelper.getCurrentScreen()) {
            "create" -> setButtonSelected(buttonCreate)
            "stats" -> setButtonSelected(buttonStats)
            else -> setButtonSelected(buttonMain)
        }
    }
}