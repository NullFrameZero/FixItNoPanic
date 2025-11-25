package com.example.fixitnopanic;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefsHelper = new SharedPrefsHelper(this);
    }

    @Override
    public void onBackPressed() {
        // Всегда возвращаемся на главный экран при нажатии "Назад"
        sharedPrefsHelper.setCurrentScreen("main");
        super.onBackPressed();
    }
}