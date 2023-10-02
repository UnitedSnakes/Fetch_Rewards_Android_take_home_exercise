package com.example.android_take_home_exercise_shanglin_yang

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create the items table with the listId column
        val createTableSQL = "CREATE TABLE items (listId INTEGER, name TEXT)"
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 数据库版本升级逻辑
        db.execSQL(UPGRADE_TABLE_SQL)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MyDatabase.db"
        private const val CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS items (" +
                "listId INTEGER," +
                "name TEXT)"

        private const val UPGRADE_TABLE_SQL = "DROP TABLE IF EXISTS items"
    }
}