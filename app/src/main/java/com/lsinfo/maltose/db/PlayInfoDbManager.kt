package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.lsinfo.maltose.model.PlayInfoModel
import com.lsinfo.maltose.model.PlayInfoStatus
import com.lsinfo.maltose.model.PlayInfoType

/**
 * Created by G on 2018-03-19.
 */
object PlayInfoDbManager {
    const val ID = "play_info_id"
    const val LAUNCHER = "launcher"
    const val DURATION = "duration"
    const val FILE_MD5 = "file_md5"
    const val STATUS = "status"
    const val TYPE = "type"

    private const val DB_TABLE = "play_info"//表名

    /**
     * 新增一条数据
     */
    @Synchronized
    fun insert(context: Context, item: PlayInfoModel?, isTransaction: Boolean = true) : Long{
        if (null == item) {
            return -1L
        }
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ID, item.playInfoId)
            values.put(LAUNCHER, item.launcher)
            values.put(DURATION, item.duration)
            values.put(FILE_MD5, item.fileMd5)
            values.put(STATUS, item.status.ordinal)
            values.put(TYPE, item.type.ordinal)
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
    @Synchronized
    fun update(context: Context, item: PlayInfoModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(ID, item.playInfoId)
            values.put(LAUNCHER, item.launcher)
            values.put(DURATION, item.duration)
            values.put(FILE_MD5, item.fileMd5)
            values.put(STATUS, item.status.ordinal)
            values.put(TYPE, item.type.ordinal)
            count = db.update(DB_TABLE, values, "$ID = ?", arrayOf(item.playInfoId))
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
    @Synchronized
    fun delete(context: Context, itemId: String, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "$ID = ?", arrayOf(itemId))
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
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): PlayInfoModel? {
        val db = DbHelper.getReadableDatabase(context)
        var cursor = db.query(DB_TABLE, null, "$ID = ?", arrayOf(itemId), null, null, null)

        var item: PlayInfoModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = PlayInfoModel(
                        playInfoId = cursor.getString(cursor.getColumnIndex(ID)),
                        launcher = cursor.getString(cursor.getColumnIndex(LAUNCHER)),
                        duration = cursor.getLong(cursor.getColumnIndex(DURATION)),
                        fileMd5 = cursor.getString(cursor.getColumnIndex(FILE_MD5)),
                        status = PlayInfoStatus.values()[cursor.getInt(cursor.getColumnIndex(STATUS))],
                        type = PlayInfoType.values()[cursor.getInt(cursor.getColumnIndex(TYPE))]
                    )
            }
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return item
    }



    /**
     * 获取所有对象
     */
    fun getAll(context: Context, autoCloseDb: Boolean = true): MutableList<PlayInfoModel> {
        val db = DbHelper.getReadableDatabase(context)

        val cursor = db.query(DB_TABLE, null, null, arrayOf(), null, null, null)

        var items : MutableList<PlayInfoModel> = mutableListOf()
        if (cursor != null) {
            while (cursor.moveToNext()){
                val item = PlayInfoModel(
                        playInfoId = cursor.getString(cursor.getColumnIndex(ID)),
                        launcher = cursor.getString(cursor.getColumnIndex(LAUNCHER)),
                        duration = cursor.getLong(cursor.getColumnIndex(DURATION)),
                        fileMd5 = cursor.getString(cursor.getColumnIndex(FILE_MD5)),
                        status = PlayInfoStatus.values()[cursor.getInt(cursor.getColumnIndex(STATUS))],
                        type = PlayInfoType.values()[cursor.getInt(cursor.getColumnIndex(TYPE))]
                )
                items.add(item)
            }
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return items
    }

    /**
     * 更新对象状态
     */
    @Synchronized
    fun updateStatus(context: Context, itemId: String, status: PlayInfoStatus, isTransaction: Boolean = true): Boolean{
        val item = get(context, itemId, isTransaction) ?: return false
        item.status = status
        return update(context, item, isTransaction) > 0
    }
}