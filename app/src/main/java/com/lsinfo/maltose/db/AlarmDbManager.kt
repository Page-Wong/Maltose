package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsinfo.maltose.model.AlarmModel
import com.lsinfo.maltose.utils.ConvertHelper
import java.util.*

/**
 * Created by G on 2018-04-27.
 */
object AlarmDbManager {
    private const val ID = "alarm_id"
    private const val SIGN = "sign"
    private const val TIME = "time"
    private const val DATE_SETTING = "date_setting"
    private const val NOTIFY_URL = "notify_url"//通知服务器执行完指令的地址
    private const val KEY = "key"//指令关键字
    private const val PARAMS = "params"//指令参数，JSON对象
    private const val CONTENT = "content"//其他参数，JSON对象

    private const val DB_TABLE = "alarm"//表名
    /**
     * 新增一条数据
     */
    fun insert(context: Context, item: AlarmModel, isTransaction: Boolean = true) : Long{
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ID, item.alarmId)
            values.put(SIGN, item.sign)
            values.put(TIME, item.time)
            values.put(DATE_SETTING, item.dateSetting)
            values.put(NOTIFY_URL, item.notifyUrl)
            values.put(KEY, item.key)
            if (item.params != null)
                values.put(PARAMS, ConvertHelper.gson.toJson(item.params))
            if (item.content != null)
                values.put(CONTENT, ConvertHelper.gson.toJson(item.content))

            id = db.insert(DB_TABLE, "", values)
            // 设置事务执行的标志为成功
            if (isTransaction) db.setTransactionSuccessful()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            if (isTransaction){
                db.endTransaction()
                //db.close()
            }
        }
        return id
    }

    /**
     * 更新一条数据
     */
    fun update(context: Context, item: AlarmModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        var count = 0
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(ID, item.alarmId)
            values.put(SIGN, item.sign)
            values.put(TIME, item.time)
            values.put(DATE_SETTING, item.dateSetting)
            values.put(NOTIFY_URL, item.notifyUrl)
            values.put(KEY, item.key)
            if (item.params != null)
                values.put(PARAMS, ConvertHelper.gson.toJson(item.params))
            if (item.content != null)
                values.put(CONTENT, ConvertHelper.gson.toJson(item.content))
            count = db.update(DB_TABLE, values, "$ID = ? ", arrayOf(item.alarmId))
            if (isTransaction) db.setTransactionSuccessful()
        } finally {
            if (isTransaction){
                db.endTransaction()
                db.close()
            }
        }
        return count
    }

    /**
     * 删除一条数据
     */
    fun delete(context: Context, itemId: String, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        var count = 0
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "$ID = ?", arrayOf(itemId))
            if (isTransaction) db.setTransactionSuccessful()
        } finally {
            if (isTransaction){
                db.endTransaction()
                db.close()
            }
        }
        return count
    }

    /**
     * 删除所有数据
     */
    fun deleteAll(context: Context, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "",arrayOf() )
            if (isTransaction) db.setTransactionSuccessful()
        } finally {
            if (isTransaction){
                db.endTransaction()
                db.close()
            }
        }
        return count
    }

    /**
     * 根据 Id 获取对象
     */
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): AlarmModel? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "$ID = ? ",
                arrayOf(itemId), null, null, null)
        var item: AlarmModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = AlarmModel(
                        alarmId = cursor.getString(cursor.getColumnIndex(ID)),
                        sign = cursor.getString(cursor.getColumnIndex(SIGN)),
                        time = cursor.getString(cursor.getColumnIndex(TIME)),
                        dateSetting = cursor.getString(cursor.getColumnIndex(DATE_SETTING)),
                        notifyUrl = cursor.getString(cursor.getColumnIndex(NOTIFY_URL)),
                        key = cursor.getString(cursor.getColumnIndex(KEY))
                )
                if (!cursor.isNull(cursor.getColumnIndex(PARAMS))) {
                    item.params = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(PARAMS)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                }
                if (!cursor.isNull(cursor.getColumnIndex(CONTENT))) {
                    item.content = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(CONTENT)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                }
            }
            cursor.close()
        }
        if (autoCloseDb) db.close()
        return item
    }

    /**
     * 获取所有对象
     */
    fun getAll(context: Context, autoCloseDb: Boolean = true): MutableList<AlarmModel> {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "",
                arrayOf(), null, null, null)
        var items : MutableList<AlarmModel> = mutableListOf()
        if (cursor != null) {
            while (cursor.moveToNext()){
                val item = AlarmModel(
                        alarmId = cursor.getString(cursor.getColumnIndex(ID)),
                        sign = cursor.getString(cursor.getColumnIndex(SIGN)),
                        time = cursor.getString(cursor.getColumnIndex(TIME)),
                        dateSetting = cursor.getString(cursor.getColumnIndex(DATE_SETTING)),
                        notifyUrl = cursor.getString(cursor.getColumnIndex(NOTIFY_URL)),
                        key = cursor.getString(cursor.getColumnIndex(KEY))
                )
                if (!cursor.isNull(cursor.getColumnIndex(PARAMS))) {
                    item.params = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(PARAMS)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                }
                if (!cursor.isNull(cursor.getColumnIndex(CONTENT))) {
                    item.content = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(CONTENT)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                }
                items.add(item)
            }
            cursor.close()
        }
        if (autoCloseDb) db.close()
        return items
    }
}