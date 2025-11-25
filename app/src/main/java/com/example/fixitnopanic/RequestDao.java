package com.example.fixitnopanic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RequestDao {
    private final DatabaseHelper dbHelper;
    private final Context context;

    public RequestDao(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    private SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    public long createRequestWithDateTime(String client, String phone, String model, String problem, String dateTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLIENT, client);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_MODEL, model);
        values.put(DatabaseHelper.COLUMN_PROBLEM, problem);
        values.put(DatabaseHelper.COLUMN_DATE_CREATED, dateTime);
        values.put(DatabaseHelper.COLUMN_STATUS, "in_progress");
        values.put(DatabaseHelper.COLUMN_DATE_COMPLETED, "");
        return db.insert(DatabaseHelper.TABLE_REQUESTS, null, values);
    }

    public List<RequestItem> getAllRequests() {
        return getRequestsByStatus(null);
    }

    public List<RequestItem> getRequestsByStatus(String status) {
        List<RequestItem> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String selection = (status != null) ? DatabaseHelper.COLUMN_STATUS + " = ?" : null;
        String[] selectionArgs = (status != null) ? new String[]{status} : null;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_REQUESTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID);
            int clientIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLIENT);
            int phoneIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE);
            int modelIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODEL);
            int problemIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROBLEM);
            int dateCreatedIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_CREATED);
            int dateCompletedIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_COMPLETED);
            int statusIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS);

            do {
                long id = cursor.getLong(idIndex);
                String client = cursor.getString(clientIndex);
                String phone = cursor.getString(phoneIndex);
                String model = cursor.getString(modelIndex);
                String problem = cursor.getString(problemIndex);
                String dateCreated = cursor.getString(dateCreatedIndex);
                String dateCompletedRaw = cursor.getString(dateCompletedIndex);
                String dateCompleted = dateCompletedRaw.isEmpty() ? null : dateCompletedRaw;
                String statusFromDb = cursor.getString(statusIndex);

                list.add(new RequestItem(id, client, phone, model, problem, dateCreated, dateCompleted, statusFromDb));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int updateRequestStatus(long id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        if ("completed".equals(status)) {
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            values.put(DatabaseHelper.COLUMN_DATE_COMPLETED, sdf.format(now.getTime()));
        } else {
            values.put(DatabaseHelper.COLUMN_DATE_COMPLETED, "");
        }
        return db.update(
                DatabaseHelper.TABLE_REQUESTS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int updateRequestData(long id, String client, String phone, String model, String problem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLIENT, client);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_MODEL, model);
        values.put(DatabaseHelper.COLUMN_PROBLEM, problem);
        return db.update(
                DatabaseHelper.TABLE_REQUESTS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteRequest(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                DatabaseHelper.TABLE_REQUESTS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int getTotalRequestsCount() {
        return getCount("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REQUESTS);
    }

    public int getInWorkRequestsCount() {
        return getCount("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REQUESTS +
                " WHERE " + DatabaseHelper.COLUMN_STATUS + " = 'in_progress'");
    }

    public int getCompletedRequestsCount() {
        return getCount("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REQUESTS +
                " WHERE " + DatabaseHelper.COLUMN_STATUS + " = 'completed'");
    }

    private int getCount(String query) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ========= ВЛОЖЕННЫЙ DatabaseHelper =========
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "fixitnopanic.db";
        private static final int DATABASE_VERSION = 2;

        static final String TABLE_REQUESTS = "requests";
        static final String COLUMN_ID = "id";
        static final String COLUMN_CLIENT = "client";
        static final String COLUMN_PHONE = "phone";
        static final String COLUMN_MODEL = "model";
        static final String COLUMN_PROBLEM = "problem";
        static final String COLUMN_DATE_CREATED = "date_created";
        static final String COLUMN_DATE_COMPLETED = "date_completed";
        static final String COLUMN_STATUS = "status";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE = "CREATE TABLE " + TABLE_REQUESTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CLIENT + " TEXT NOT NULL, " +
                    COLUMN_PHONE + " TEXT NOT NULL, " +
                    COLUMN_MODEL + " TEXT NOT NULL, " +
                    COLUMN_PROBLEM + " TEXT NOT NULL, " +
                    COLUMN_DATE_CREATED + " TEXT NOT NULL, " +
                    COLUMN_DATE_COMPLETED + " TEXT, " +
                    COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'in_progress'" +
                    ")";
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_REQUESTS + " ADD COLUMN " + COLUMN_PHONE + " TEXT NOT NULL DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_REQUESTS + " ADD COLUMN " + COLUMN_PROBLEM + " TEXT NOT NULL DEFAULT ''");
            }
        }
    }
}