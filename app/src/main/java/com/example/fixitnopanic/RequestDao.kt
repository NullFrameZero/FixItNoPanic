package com.example.fixitnopanic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class RequestDao(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase get() = dbHelper.writableDatabase

    fun createRequestWithDateTime(client: String, phone: String, model: String, problem: String, dateTime: String): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CLIENT, client)
            put(DatabaseHelper.COLUMN_PHONE, phone)
            put(DatabaseHelper.COLUMN_MODEL, model)
            put(DatabaseHelper.COLUMN_PROBLEM, problem)
            put(DatabaseHelper.COLUMN_DATE_CREATED, dateTime)
            put(DatabaseHelper.COLUMN_STATUS, "in_progress")
            put(DatabaseHelper.COLUMN_DATE_COMPLETED, "")
        }
        return db.insert(DatabaseHelper.TABLE_REQUESTS, null, values)
    }

    fun getAllRequests(): List<RequestItem> = getRequestsByStatus(null)

    fun getRequestsByStatus(status: String?): List<RequestItem> {
        val list = mutableListOf<RequestItem>()
        val selection = if (status != null) "${DatabaseHelper.COLUMN_STATUS} = ?" else null
        val selectionArgs = if (status != null) arrayOf(status) else null
        val cursor = db.query(
            DatabaseHelper.TABLE_REQUESTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_ID} DESC"
        )
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)
            val clientIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLIENT)
            val phoneIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)
            val modelIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODEL)
            val problemIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROBLEM)
            val dateCreatedIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_CREATED)
            val dateCompletedIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_COMPLETED)
            val statusIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)
            do {
                val id = cursor.getLong(idIndex)
                val client = cursor.getString(clientIndex)
                val phone = cursor.getString(phoneIndex)
                val model = cursor.getString(modelIndex)
                val problem = cursor.getString(problemIndex)
                val dateCreated = cursor.getString(dateCreatedIndex)
                val dateCompletedRaw = cursor.getString(dateCompletedIndex)
                val dateCompleted = if (dateCompletedRaw.isEmpty()) null else dateCompletedRaw
                val statusFromDb = cursor.getString(statusIndex)
                list.add(RequestItem(id, client, phone, model, problem, dateCreated, dateCompleted, statusFromDb))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateRequestStatus(id: Long, status: String): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, status)
            if (status == "completed") {
                val currentDate = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val completedDate = dateFormat.format(currentDate)
                put(DatabaseHelper.COLUMN_DATE_COMPLETED, completedDate)
            } else {
                put(DatabaseHelper.COLUMN_DATE_COMPLETED, "")
            }
        }
        return db.update(
            DatabaseHelper.TABLE_REQUESTS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun updateRequestData(id: Long, client: String, phone: String, model: String, problem: String): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CLIENT, client)
            put(DatabaseHelper.COLUMN_PHONE, phone)
            put(DatabaseHelper.COLUMN_MODEL, model)
            put(DatabaseHelper.COLUMN_PROBLEM, problem)
        }
        return db.update(
            DatabaseHelper.TABLE_REQUESTS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteRequest(id: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_REQUESTS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getTotalRequestsCount(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REQUESTS}", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getInWorkRequestsCount(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REQUESTS} WHERE ${DatabaseHelper.COLUMN_STATUS} = 'in_progress'", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getCompletedRequestsCount(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REQUESTS} WHERE ${DatabaseHelper.COLUMN_STATUS} = 'completed'", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            const val DATABASE_NAME = "fixitnopanic.db"
            const val DATABASE_VERSION = 2
            const val TABLE_REQUESTS = "requests"
            const val COLUMN_ID = "id"
            const val COLUMN_CLIENT = "client"
            const val COLUMN_PHONE = "phone"
            const val COLUMN_MODEL = "model"
            const val COLUMN_PROBLEM = "problem"
            const val COLUMN_DATE_CREATED = "date_created"
            const val COLUMN_DATE_COMPLETED = "date_completed"
            const val COLUMN_STATUS = "status"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val createTable = """
                CREATE TABLE $TABLE_REQUESTS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CLIENT TEXT NOT NULL,
                    $COLUMN_PHONE TEXT NOT NULL,
                    $COLUMN_MODEL TEXT NOT NULL,
                    $COLUMN_PROBLEM TEXT NOT NULL,
                    $COLUMN_DATE_CREATED TEXT NOT NULL,
                    $COLUMN_DATE_COMPLETED TEXT,
                    $COLUMN_STATUS TEXT NOT NULL DEFAULT 'in_progress'
                )
            """.trimIndent()
            db.execSQL(createTable)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE $TABLE_REQUESTS ADD COLUMN $COLUMN_PHONE TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_REQUESTS ADD COLUMN $COLUMN_PROBLEM TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}