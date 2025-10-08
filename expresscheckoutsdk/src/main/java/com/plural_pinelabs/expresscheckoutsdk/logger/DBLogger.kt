package com.plural_pinelabs.expresscheckoutsdk.logger

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBLogger private constructor(context: Context) : SQLiteOpenHelper(context, "logs.db", null, 1) {

    companion object {
        @Volatile
        private var instance: DBLogger? = null

        fun getInstance(context: Context): DBLogger {
            return instance ?: synchronized(this) {
                instance ?: DBLogger(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                json TEXT,
                timestamp INTEGER,
                synced INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS logs")
        onCreate(db)
    }
}
