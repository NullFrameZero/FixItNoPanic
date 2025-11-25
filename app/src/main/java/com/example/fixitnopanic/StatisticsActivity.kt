package com.example.fixitnopanic

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback

class StatisticsActivity : AppCompatActivity() {
    private lateinit var requestDao: RequestDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var buttonBack: ImageButton
    private lateinit var textTotalRequests: TextView
    private lateinit var textInWorkRequests: TextView
    private lateinit var textCompletedRequests: TextView
    private lateinit var requestsInWorkContainer: LinearLayout
    private lateinit var requestsCompletedContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        requestDao = RequestDao(this)
        sharedPrefsHelper = SharedPrefsHelper(this)
        sharedPrefsHelper.setCurrentScreen("stats")

        buttonBack = findViewById(R.id.buttonBack)
        textTotalRequests = findViewById(R.id.textTotalRequests)
        textInWorkRequests = findViewById(R.id.textInWorkRequests)
        textCompletedRequests = findViewById(R.id.textCompletedRequests)
        requestsInWorkContainer = findViewById(R.id.requestsInWorkContainer)
        requestsCompletedContainer = findViewById(R.id.requestsCompletedContainer)

        buttonBack.setOnClickListener {
            sharedPrefsHelper.setCurrentScreen("main")
            finish()
        }

        updateStatistics()
        loadRequestsInWork()
        loadCompletedRequests()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                sharedPrefsHelper.setCurrentScreen("main")
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateStatistics()
        loadRequestsInWork()
        loadCompletedRequests()
    }

    private fun updateStatistics() {
        textTotalRequests.text = requestDao.getTotalRequestsCount().toString()
        textInWorkRequests.text = requestDao.getInWorkRequestsCount().toString()
        textCompletedRequests.text = requestDao.getCompletedRequestsCount().toString()
    }

    private fun loadRequestsInWork() {
        requestsInWorkContainer.removeAllViews()
        val requests = requestDao.getRequestsByStatus("in_progress")
        if (requests.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Нет заявок в работе"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 16f
                gravity = View.TEXT_ALIGNMENT_CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 16 }
            }
            requestsInWorkContainer.addView(emptyView)
        } else {
            for (request in requests) {
                val itemView = layoutInflater.inflate(R.layout.item_request_statistic, requestsInWorkContainer, false)
                itemView.findViewById<TextView>(R.id.textViewId).text = request.id.toString()
                itemView.findViewById<TextView>(R.id.textViewClient).text = request.client
                itemView.findViewById<TextView>(R.id.textViewModel).text = request.model
                itemView.findViewById<TextView>(R.id.textViewProblem).text = request.problem
                itemView.findViewById<TextView>(R.id.textViewDateCreated).text = request.dateCreated.substringBefore(' ')
                val dateCompletedTextView = itemView.findViewById<TextView>(R.id.textViewDateCompleted)
                if (request.dateCompleted != null) {
                    dateCompletedTextView.text = request.dateCompleted.substringBefore(' ')
                    dateCompletedTextView.visibility = View.VISIBLE
                } else {
                    dateCompletedTextView.visibility = View.GONE
                }
                val statusTextView = itemView.findViewById<TextView>(R.id.textViewStatus)
                statusTextView.text = if (request.status == "in_progress") "в работе" else "выполнено"
                statusTextView.setBackgroundResource(
                    if (request.status == "in_progress") R.drawable.status_badge_background
                    else R.drawable.status_completed_badge_background
                )
                requestsInWorkContainer.addView(itemView)
                val line = View(this).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3)
                    setBackgroundColor(Color.parseColor("#444444"))
                }
                requestsInWorkContainer.addView(line)
            }
        }
    }

    private fun loadCompletedRequests() {
        requestsCompletedContainer.removeAllViews()
        val requests = requestDao.getRequestsByStatus("completed")
        if (requests.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Нет выполненных заявок"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 16f
                gravity = View.TEXT_ALIGNMENT_CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 16 }
            }
            requestsCompletedContainer.addView(emptyView)
        } else {
            for (request in requests) {
                val itemView = layoutInflater.inflate(R.layout.item_request_statistic, requestsCompletedContainer, false)
                itemView.findViewById<TextView>(R.id.textViewId).text = request.id.toString()
                itemView.findViewById<TextView>(R.id.textViewClient).text = request.client
                itemView.findViewById<TextView>(R.id.textViewModel).text = request.model
                itemView.findViewById<TextView>(R.id.textViewProblem).text = request.problem
                itemView.findViewById<TextView>(R.id.textViewDateCreated).text = request.dateCreated.substringBefore(' ')
                val dateCompletedTextView = itemView.findViewById<TextView>(R.id.textViewDateCompleted)
                if (request.dateCompleted != null) {
                    dateCompletedTextView.text = request.dateCompleted.substringBefore(' ')
                    dateCompletedTextView.visibility = View.VISIBLE
                } else {
                    dateCompletedTextView.visibility = View.GONE
                }
                val statusTextView = itemView.findViewById<TextView>(R.id.textViewStatus)
                statusTextView.text = "выполнено"
                statusTextView.setBackgroundResource(R.drawable.status_completed_badge_background)
                requestsCompletedContainer.addView(itemView)
                val line = View(this).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3)
                    setBackgroundColor(Color.parseColor("#444444"))
                }
                requestsCompletedContainer.addView(line)
            }
        }
    }
}