package com.lsinfo.maltose.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by G on 2018-03-16.
 */
class DbHelper private constructor(val context: Context): SQLiteOpenHelper(context, "wsservice.db",  null, 19) {
    companion object {
        private var instance: DbHelper? = null

        @Synchronized
        private fun getInstance(c: Context): DbHelper {
            if (instance == null) instance = DbHelper(c)
            return instance!!
        }

        @Synchronized
        fun getReadableDatabase(context: Context): SQLiteDatabase {
            return getInstance(context).readableDatabase
        }

        @Synchronized
        fun getWritableDatabase(context: Context): SQLiteDatabase {
            return getInstance(context).writableDatabase
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        //指令表
        db.execSQL("CREATE TABLE IF NOT EXISTS instruction(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "original TEXT, " +
                "instruction_id TEXT unique, " +
                "notify_url TEXT, " +
                "key TEXT, " +
                "sign TEXT," +
                "timestamp TEXT, " +
                "params TEXT, " +
                "content TEXT," +
                "result TEXT," +
                "status INT)")

        //操作表
        db.execSQL("CREATE TABLE IF NOT EXISTS operation(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "operation_id TEXT unique," +
                "instruction_id TEXT," +
                "start_time TEXT," +
                "finish_time TEXT," +
                "key TEXT," +
                "type INT," +
                "method TEXT," +
                "params TEXT," +
                "content TEXT," +
                "result TEXT," +
                "status INT)")

        //操作字典表
        db.execSQL("CREATE TABLE IF NOT EXISTS operation_dictionary(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "key TEXT unique," +
                "type INT," +
                "method TEXT)")

        //节目列表
        db.execSQL("CREATE TABLE IF NOT EXISTS play_info(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "play_info_id TEXT," +
                "player_id TEXT," +
                "launcher TEXT," +
                "duration INT," +
                "file_md5 TEXT," +
                "type INT," +
                "status INT)")

        //播放器节目关系表
        db.execSQL("CREATE TABLE IF NOT EXISTS player_play_info(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sort INT," +
                "play_info_id TEXT," +
                "player_id TEXT)")

        //播放器列表
        db.execSQL("CREATE TABLE IF NOT EXISTS player(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_id TEXT unique," +
                "sort INT unique," +
                "width INT," +
                "height INT," +
                "x INT," +
                "y INT)")

        //定时操作闹钟列表
        db.execSQL("CREATE TABLE IF NOT EXISTS alarm(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "alarm_id TEXT unique," +
                "sign TEXT," +
                "time TEXT," +
                "notify_url TEXT, " +
                "date_setting TEXT," +
                "key TEXT," +
                "params TEXT," +
                "content TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropAllTables(db)
        onCreate(db)
    }

    private fun dropAllTables(db: SQLiteDatabase) {
        db.execSQL("drop table if exists instruction")
        db.execSQL("drop table if exists operation")
        db.execSQL("drop table if exists operation_dictionary")
        db.execSQL("drop table if exists play_info")
        db.execSQL("drop table if exists player")
        db.execSQL("drop table if exists alarm")

        onCreate(db)
    }
}