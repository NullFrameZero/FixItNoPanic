package com.example.fixitnopanic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {

    private RequestDao requestDao;
    private SharedPrefsHelper sharedPrefsHelper;
    private ImageButton buttonBack;
    private TextView textTotalRequests;
    private TextView textInWorkRequests;
    private TextView textCompletedRequests;
    private LinearLayout requestsInWorkContainer;
    private LinearLayout requestsCompletedContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        requestDao = new RequestDao(this);
        sharedPrefsHelper = new SharedPrefsHelper(this);
        sharedPrefsHelper.setCurrentScreen("stats");

        buttonBack = findViewById(R.id.buttonBack);
        textTotalRequests = findViewById(R.id.textTotalRequests);
        textInWorkRequests = findViewById(R.id.textInWorkRequests);
        textCompletedRequests = findViewById(R.id.textCompletedRequests);
        requestsInWorkContainer = findViewById(R.id.requestsInWorkContainer);
        requestsCompletedContainer = findViewById(R.id.requestsCompletedContainer);

        buttonBack.setOnClickListener(v -> {
            sharedPrefsHelper.setCurrentScreen("main");
            finish();
        });

        updateStatistics();
        loadRequestsInWork();
        loadCompletedRequests();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sharedPrefsHelper.setCurrentScreen("main");
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatistics();
        loadRequestsInWork();
        loadCompletedRequests();
    }

    private void updateStatistics() {
        int total = requestDao.getTotalRequestsCount();
        int inWork = requestDao.getInWorkRequestsCount();
        int completed = requestDao.getCompletedRequestsCount();
        textTotalRequests.setText(String.valueOf(total));
        textInWorkRequests.setText(String.valueOf(inWork));
        textCompletedRequests.setText(String.valueOf(completed));
    }

    private void loadRequestsInWork() {
        requestsInWorkContainer.removeAllViews();
        java.util.List<RequestItem> requests = requestDao.getRequestsByStatus("in_progress");
        if (requests.isEmpty()) {
            addEmptyView(requestsInWorkContainer, "Нет заявок в работе");
        } else {
            addRequestViews(requestsInWorkContainer, requests);
        }
    }

    private void loadCompletedRequests() {
        requestsCompletedContainer.removeAllViews();
        java.util.List<RequestItem> requests = requestDao.getRequestsByStatus("completed");
        if (requests.isEmpty()) {
            addEmptyView(requestsCompletedContainer, "Нет выполненных заявок");
        } else {
            for (RequestItem request : requests) {
                View itemView = getLayoutInflater().inflate(R.layout.item_request_statistic, requestsCompletedContainer, false);
                bindRequestView(itemView, request, true);
                requestsCompletedContainer.addView(itemView);
                addDivider(requestsCompletedContainer);
            }
        }
    }

    private void addEmptyView(LinearLayout container, String text) {
        TextView emptyView = new TextView(this);
        emptyView.setText(text);
        emptyView.setTextColor(Color.parseColor("#CCCCCC"));
        emptyView.setTextSize(16);
        emptyView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 16;
        emptyView.setLayoutParams(params);
        container.addView(emptyView);
    }

    private void addRequestViews(LinearLayout container, java.util.List<RequestItem> requests) {
        for (RequestItem request : requests) {
            View itemView = getLayoutInflater().inflate(R.layout.item_request_statistic, container, false);
            bindRequestView(itemView, request, false);
            container.addView(itemView);
            addDivider(container);
        }
    }

    private void bindRequestView(View itemView, RequestItem request, boolean forceCompleted) {
        TextView id = itemView.findViewById(R.id.textViewId);
        TextView client = itemView.findViewById(R.id.textViewClient);
        TextView model = itemView.findViewById(R.id.textViewModel);
        TextView problem = itemView.findViewById(R.id.textViewProblem);
        TextView dateCreated = itemView.findViewById(R.id.textViewDateCreated);
        TextView dateCompleted = itemView.findViewById(R.id.textViewDateCompleted);
        TextView status = itemView.findViewById(R.id.textViewStatus);

        id.setText(String.valueOf(request.getId()));
        client.setText(request.getClient());
        model.setText(request.getModel());
        problem.setText(request.getProblem());
        dateCreated.setText(request.getDateCreated().split(" ")[0]);

        if (request.getDateCompleted() != null && !request.getDateCompleted().isEmpty()) {
            dateCompleted.setText(request.getDateCompleted().split(" ")[0]);
            dateCompleted.setVisibility(View.VISIBLE);
        } else {
            dateCompleted.setVisibility(View.GONE);
        }

        String statusText = forceCompleted ? "выполнено" :
                "in_progress".equals(request.getStatus()) ? "в работе" : "выполнено";
        status.setText(statusText);
        int badge = "in_progress".equals(request.getStatus()) && !forceCompleted
                ? R.drawable.status_badge_background
                : R.drawable.status_completed_badge_background;
        status.setBackgroundResource(badge);
    }

    private void addDivider(LinearLayout container) {
        View line = new View(this);
        line.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 3
        ));
        line.setBackgroundColor(Color.parseColor("#444444"));
        container.addView(line);
    }
}