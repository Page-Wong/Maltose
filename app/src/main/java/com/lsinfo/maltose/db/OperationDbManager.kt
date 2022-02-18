package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsinfo.maltose.Config.DATE_TIME_FORMAT_PATTERN
import com.lsinfo.maltose.bean.*
import com.lsinfo.maltose.model.OperateType
import com.lsinfo.maltose.model.OperationModel
import com.lsinfo.maltose.model.OperationStatus
import com.lsinfo.maltose.utils.ConvertHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by G on 2018-03-19.
 */
object OperationDbManager {
    private const val _ID = "_id"
    private const val OPERATION_ID = "operation_id" //该操作唯一ID
    private const val INSTRUCTION_ID = "instruction_id" //指令ID
    private const val START_TIME = "start_time" //操作开始时间
    private const val FINISH_TIME = "finish_time" //操作结束时间
    private const val KEY = "key"//指令关键字
    private const val TYPE = "type" //操作类型
    private const val METHOD = "method" //调用的方法
    private const val PARAMS = "params" //操作方法参数，JSON对象
    private const val CONTENT = "content" //其他参数，JSON对象
    private const val RESULT = "result" //操作结果
    private const val STATUS = "status" //操作状态

    private const val DB_TABLE = "operation"//表名

    /**
     * 新增一条数据
     */
    fun insert(context: Context, item: OperationModel?, isTransaction: Boolean = true) : Long{
        if (null == item) {
            return -1L
        }
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(OPERATION_ID, item.operationId)
            values.put(INSTRUCTION_ID, item.instructionId)
            values.put(START_TIME, SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).format(item.startTime))
            if (item.finishTime != null)
                values.put(FINISH_TIME, SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).format(item.finishTime))
            values.put(TYPE, item.type.ordinal)
            values.put(KEY, item.key)
            values.put(METHOD, item.method)
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
    fun update(context: Context, item: OperationModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        var count: Int
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(INSTRUCTION_ID, item.instructionId)
            values.put(START_TIME, SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).format(item.startTime))
            if (item.finishTime != null)
                values.put(FINISH_TIME, SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).format(item.finishTime))
            values.put(TYPE, item.type.ordinal)
            values.put(KEY, item.key)
            values.put(METHOD, item.method)
            if (item.params != null)
                values.put(PARAMS, ConvertHelper.gson.toJson(item.params))
            if (item.content != null)
                values.put(CONTENT, ConvertHelper.gson.toJson(item.content))
            if (item.result != null)
                values.put(RESULT, item.result?.toString())
            values.put(STATUS, item.status.ordinal)
            count = db.update(DB_TABLE, values, "${OPERATION_ID} = ?", arrayOf(item.operationId))
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
        var count: Int
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "${OPERATION_ID} = ?", arrayOf(itemId))
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
    @Synchronized
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
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): OperationModel? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "${OPERATION_ID} = ?",
                arrayOf(itemId), null, null, null)
        var item: OperationModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = OperationModel(
                        operationId = cursor.getString(cursor.getColumnIndex(OPERATION_ID)),
                        instructionId = cursor.getString(cursor.getColumnIndex(INSTRUCTION_ID)),
                        startTime = SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).parse(cursor.getString(cursor.getColumnIndex(START_TIME))),
                        key = cursor.getString(cursor.getColumnIndex(KEY)),
                        type = OperateType.values()[cursor.getInt(cursor.getColumnIndex(TYPE))],
                        method = cursor.getString(cursor.getColumnIndex(METHOD)),
                        status = OperationStatus.values()[cursor.getInt(cursor.getColumnIndex(STATUS))]
                        )
                if (!cursor.isNull(cursor.getColumnIndex(FINISH_TIME)))
                    item.finishTime = SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).parse(cursor.getString(cursor.getColumnIndex(FINISH_TIME)))
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
}