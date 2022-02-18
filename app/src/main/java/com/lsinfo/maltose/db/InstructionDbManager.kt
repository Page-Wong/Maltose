package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.model.InstructionModel
import com.lsinfo.maltose.model.InstructionStatus
import com.lsinfo.maltose.utils.ConvertHelper
import java.lang.reflect.Type
import java.util.*

/**
 * Created by G on 2018-03-19.
 */
object InstructionDbManager {
    private const val _ID = "_id"
    private const val ORIGINAL = "original"//原始指令字符串
    private const val INSTRUCTION_ID = "instruction_id"//指令唯一ID
    private const val NOTIFY_URL = "notify_url"//通知服务器执行完指令的地址
    private const val KEY = "key"//指令关键字
    private const val SIGN = "sign"//服务器签名
    private const val TIMESTAMP = "timestamp"//指令时间
    private const val PARAMS = "params"//指令参数，JSON对象
    private const val CONTENT = "content"//其他参数，JSON对象
    private const val RESULT = "result" //操作结果
    private const val STATUS = "status"//指令状态

    private const val DB_TABLE = "instruction"//表名

    /**
     * 新增一条数据
     */
    fun insert(context: Context, item: InstructionModel?, isTransaction: Boolean = true) : Long{
        if (null == item) {
            return -1L
        }
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ORIGINAL, item.original)
            values.put(INSTRUCTION_ID, item.instructionId)
            values.put(NOTIFY_URL, item.notifyUrl)
            values.put(KEY, item.key)
            values.put(SIGN, item.sign)
            values.put(TIMESTAMP, item.timestamp)
            if (item.params != null)
                values.put(PARAMS, ConvertHelper.gson.toJson(item.params))
            if (item.content != null)
                values.put(CONTENT, ConvertHelper.gson.toJson(item.content))
            if (item.result != null)
                values.put(RESULT, item.result?.toString())
            values.put(STATUS, item.status.ordinal)
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
    fun update(context: Context, item: InstructionModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(NOTIFY_URL, item.notifyUrl)
            values.put(KEY, item.key)
            values.put(SIGN, item.sign)
            values.put(TIMESTAMP, item.timestamp)
            if (item.params != null)
                values.put(PARAMS, ConvertHelper.gson.toJson(item.params))
            if (item.content != null)
                values.put(CONTENT, ConvertHelper.gson.toJson(item.content))
            if (item.result != null)
                values.put(RESULT, item.result?.toString())
            values.put(STATUS, item.status.ordinal)
            count = db.update(DB_TABLE, values, "${INSTRUCTION_ID} = ?", arrayOf(item.instructionId))
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
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "${INSTRUCTION_ID} = ?", arrayOf(itemId))
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
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): InstructionModel? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "${INSTRUCTION_ID} = ?",
                arrayOf(itemId), null, null, null)
        var item: InstructionModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = InstructionModel(
                        original = cursor.getString(cursor.getColumnIndex(ORIGINAL)),
                        instructionId = cursor.getString(cursor.getColumnIndex(INSTRUCTION_ID)),
                        notifyUrl = cursor.getString(cursor.getColumnIndex(NOTIFY_URL)),
                        key = cursor.getString(cursor.getColumnIndex(KEY)),
                        sign = cursor.getString(cursor.getColumnIndex(SIGN)),
                        timestamp =  cursor.getLong(cursor.getColumnIndex(TIMESTAMP)),
                        status = InstructionStatus.values()[cursor.getInt(cursor.getColumnIndex(STATUS))]
                    )

                if (!cursor.isNull(cursor.getColumnIndex(PARAMS)))
                    item.params = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(PARAMS)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                if (!cursor.isNull(cursor.getColumnIndex(CONTENT)))
                    item.content = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(CONTENT)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                if (!cursor.isNull(cursor.getColumnIndex(RESULT)))
                    item.result = ResultBean().fromString(cursor.getString(cursor.getColumnIndex(RESULT)))//ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(RESULT)), ResultBean::class.java)
            }
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return item
    }

    /**
     * 查找已完成但未反馈的指令
     */
    fun findComplete(context: Context, autoCloseDb: Boolean = true): MutableList<InstructionModel> {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "${STATUS} = ?",
                arrayOf(InstructionStatus.COMPLETE.ordinal.toString()), null, null, null)
        var items : MutableList<InstructionModel> = mutableListOf()
        if (cursor != null) {
            while (cursor.moveToNext()){
                val item = InstructionModel(
                        original = cursor.getString(cursor.getColumnIndex(ORIGINAL)),
                        instructionId = cursor.getString(cursor.getColumnIndex(INSTRUCTION_ID)),
                        notifyUrl = cursor.getString(cursor.getColumnIndex(NOTIFY_URL)),
                        key = cursor.getString(cursor.getColumnIndex(KEY)),
                        sign = cursor.getString(cursor.getColumnIndex(SIGN)),
                        timestamp =  cursor.getLong(cursor.getColumnIndex(TIMESTAMP)),
                        status = InstructionStatus.values()[cursor.getInt(cursor.getColumnIndex(STATUS))]
                )
                if (!cursor.isNull(cursor.getColumnIndex(PARAMS)))
                    item.params = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(PARAMS)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                if (!cursor.isNull(cursor.getColumnIndex(CONTENT)))
                    item.content = ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(CONTENT)), object : TypeToken<SortedMap<String, String?>>() {}.type)
                if (!cursor.isNull(cursor.getColumnIndex(RESULT)))
                    item.result = ResultBean().fromString(cursor.getString(cursor.getColumnIndex(RESULT)))//ConvertHelper.gson.fromJson(cursor.getString(cursor.getColumnIndex(RESULT)), ResultBean::class.java)
                items.add(item)
            }
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return items
    }

}