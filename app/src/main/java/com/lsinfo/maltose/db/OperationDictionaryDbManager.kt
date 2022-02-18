package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.lsinfo.maltose.bean.OperationDictionaryBean
import com.lsinfo.maltose.model.OperateType

/**
 * Created by G on 2018-03-19.
 */
object OperationDictionaryDbManager {
    private const val _ID = "_id"
    private const val KEY = "key"//指令关键字
    private const val TYPE = "type"//操作类型
    private const val METHOND = "method"//调用的方法

    private const val DB_TABLE = "operation_dictionary"//表名
            /**
     * 新增一条数据
     */
    fun insert(context: Context, item: OperationDictionaryBean, isTransaction: Boolean = true) : Long{
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY, item.key)
            values.put(TYPE, item.type.ordinal)
            values.put(METHOND, item.method)

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
    fun update(context: Context, item: OperationDictionaryBean, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        var count = 0
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(TYPE, item.type.ordinal)
            values.put(METHOND, item.method)
            count = db.update(DB_TABLE, values, "$KEY = ? ", arrayOf(item.key))
            if (isTransaction) db.setTransactionSuccessful()
        } finally {
            if (isTransaction){
                db.endTransaction()
                //db.close()
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
            count = db.delete(DB_TABLE, "$KEY = ?", arrayOf(itemId))
            if (isTransaction) db.setTransactionSuccessful()
        } finally {
            if (isTransaction){
                db.endTransaction()
                //db.close()
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
                //db.close()
            }
        }
        return count
    }

    /**
     * 根据 Id 获取对象
     */
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): OperationDictionaryBean? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "$KEY = ? ",
                arrayOf(itemId), null, null, null)
        var item: OperationDictionaryBean? = null
        if (cursor != null) {
            if (cursor.moveToFirst())
                item = OperationDictionaryBean(
                        key = cursor.getString(cursor.getColumnIndex(KEY)),
                        type = OperateType.values()[cursor.getInt(cursor.getColumnIndex(TYPE))],
                        method = cursor.getString(cursor.getColumnIndex(METHOND))
                        )
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return item
    }

}