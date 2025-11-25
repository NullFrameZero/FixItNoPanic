package com.example.fixitnopanic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RequestDao requestDao;
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private TextView timeTextView;
    private SharedPrefsHelper sharedPrefsHelper;
    private Button buttonMain;
    private Button buttonCreate;
    private Button buttonStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestDao = new RequestDao(this);
        sharedPrefsHelper = new SharedPrefsHelper(this);
        timeTextView = findViewById(R.id.timeTextView);
        recyclerView = findViewById(R.id.recyclerViewRequests);
        buttonMain = findViewById(R.id.buttonMain);
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonStats = findViewById(R.id.buttonStats);

        buttonMain.setEnabled(false);
        setButtonSelected(buttonMain);

        updateTime();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(requestDao.getAllRequests(), this, requestDao);
        recyclerView.setAdapter(adapter);

        // ✅ ИСПОЛЬЗУЕМ СТАНДАРТНЫЙ DividerItemDecoration из AndroidX
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_line));
        recyclerView.addItemDecoration(divider);

        sharedPrefsHelper.setCurrentScreen("main");

        buttonMain.setOnClickListener(v -> setButtonSelected(buttonMain));
        buttonCreate.setOnClickListener(v -> {
            setButtonSelected(buttonCreate);
            startActivity(new Intent(this, CreateRequestActivity.class));
        });
        buttonStats.setOnClickListener(v -> {
            setButtonSelected(buttonStats);
            startActivity(new Intent(this, StatisticsActivity.class));
        });

        loadRequests();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String current = sharedPrefsHelper.getCurrentScreen();
                if (!"main".equals(current)) {
                    sharedPrefsHelper.setCurrentScreen("main");
                    updateButtonSelection();
                } else {
                    setEnabled(false);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTime();
        loadRequests();
        updateButtonSelection();
    }

    private void updateTime() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy, HH:mm", Locale.getDefault());
        timeTextView.setText(format.format(now.getTime()));
    }

    private void loadRequests() {
        adapter.updateData(requestDao.getAllRequests());
    }

    private void setButtonSelected(Button selected) {
        resetButtonStyle(buttonMain);
        resetButtonStyle(buttonCreate);
        resetButtonStyle(buttonStats);

        selected.setBackgroundResource(R.drawable.btn_nav_selected);
        selected.setTextColor(Color.WHITE);
        selected.setEnabled(selected.getId() != R.id.buttonMain);

        String screen;
        if (selected.getId() == R.id.buttonCreate) {
            screen = "create";
        } else if (selected.getId() == R.id.buttonStats) {
            screen = "stats";
        } else {
            screen = "main";
        }
        sharedPrefsHelper.setCurrentScreen(screen);
    }

    private void resetButtonStyle(Button button) {
        button.setBackgroundResource(R.drawable.btn_nav_selector);
        button.setTextColor(Color.parseColor("#4CAF50"));
        button.setEnabled(true);
    }

    private void updateButtonSelection() {
        String current = sharedPrefsHelper.getCurrentScreen();
        if ("create".equals(current)) {
            setButtonSelected(buttonCreate);
        } else if ("stats".equals(current)) {
            setButtonSelected(buttonStats);
        } else {
            setButtonSelected(buttonMain);
        }
    }
}