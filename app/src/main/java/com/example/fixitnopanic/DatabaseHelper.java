package com.example.fixitnopanic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fixit.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_REQUESTS = "requests";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CLIENT = "client";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_PROBLEM = "problem";
    public static final String COLUMN_DATE_CREATED = "date_created";
    public static final String COLUMN_DATE_COMPLETED = "date_completed";
    public static final String COLUMN_STATUS = "status";

    private static final String CREATE_TABLE_REQUESTS =
            "CREATE TABLE " + TABLE_REQUESTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CLIENT + " TEXT NOT NULL, " +
                    COLUMN_MODEL + " TEXT NOT NULL, " +
                    COLUMN_PROBLEM + " TEXT NOT NULL, " +
                    COLUMN_DATE_CREATED + " TEXT NOT NULL, " +
                    COLUMN_DATE_COMPLETED + " TEXT, " +
                    COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'in_progress'" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_REQUESTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS);
        onCreate(db);
    }
}