package com.example.customalarm.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DBNAME, null, VERSION) {

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME (id integer PRIMARY KEY, title text );")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME;")
        onCreate(db)
    }

    companion object {
        const val TABLE_NAME = "alarm_setting"
        private const val DBNAME = "custom_alarm_db.sqlite"
        private const val VERSION = 1
        private var helper: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (helper == null) {
                helper = DatabaseHelper(context)
            }
            return helper!!
        }
    }
}
