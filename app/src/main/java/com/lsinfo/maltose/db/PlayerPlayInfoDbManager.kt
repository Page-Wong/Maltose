package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.lsinfo.maltose.model.*

/**
 * Created by G on 2018-03-19.
 */
object PlayerPlayInfoDbManager {
    const val PLAYER_ID = "player_id"
    const val PLAY_INFO_ID = "play_info_id"
    const val SORT = "sort"

    private const val DB_TABLE = "player_play_info"//表名

    /**
     * 新增一条数据
     */
    fun insert(context: Context, item: PlayerPlayInfoModel?, isTransaction: Boolean = true) : Long{
        if (null == item) {
            return -1L
        }
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(PLAYER_ID, item.playerId)
            values.put(PLAY_INFO_ID, item.playInfoId)
            values.put(SORT, item.sort)
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
    fun update(context: Context, item: PlayerPlayInfoModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(PLAYER_ID, item.playerId)
            values.put(PLAY_INFO_ID, item.playInfoId)
            values.put(SORT, item.sort)
            count = db.update(DB_TABLE, values, "$PLAYER_ID = ? AND $PLAY_INFO_ID = ?", arrayOf(item.playerId, item.playInfoId))
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
    fun delete(context: Context, playerId: String, playInfoId: String, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            count = db.delete(DB_TABLE, "$PLAYER_ID = ? AND $PLAY_INFO_ID = ?", arrayOf(playerId, playInfoId))
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
    fun get(context: Context, playerId: String, playInfoId: String, sort: Int, autoCloseDb: Boolean = true): PlayerPlayInfoModel? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "$PLAYER_ID = ? AND $PLAY_INFO_ID = ? AND $SORT = ?",
                arrayOf(playerId, playInfoId, sort.toString()), null, null, null)
        var item: PlayerPlayInfoModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = PlayerPlayInfoModel(
                        playerId = cursor.getString(cursor.getColumnIndex(PLAYER_ID)),
                        playInfoId = cursor.getString(cursor.getColumnIndex(PLAY_INFO_ID)),
                        sort = cursor.getInt(cursor.getColumnIndex(SORT))
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
    fun getAll(context: Context, autoCloseDb: Boolean = true): MutableList<PlayerPlayInfoModel> {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "",
                arrayOf(), null, null, SORT)
        var items : MutableList<PlayerPlayInfoModel> = mutableListOf()
        if (cursor != null) {
            while (cursor.moveToNext()){
                val item = PlayerPlayInfoModel(
                        playerId = cursor.getString(cursor.getColumnIndex(PLAYER_ID)),
                        playInfoId = cursor.getString(cursor.getColumnIndex(PLAY_INFO_ID)),
                        sort = cursor.getInt(cursor.getColumnIndex(SORT))
                )
                items.add(item)
            }
            cursor.close()
        }
        //if (autoCloseDb) db.close()
        return items
    }
}