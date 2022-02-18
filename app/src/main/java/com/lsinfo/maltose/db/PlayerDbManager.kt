package com.lsinfo.maltose.db

import android.content.ContentValues
import android.content.Context
import com.lsinfo.maltose.model.PlayerModel

/**
 * Created by G on 2018-03-19.
 */
object PlayerDbManager {
    const val ID = "player_id"
    const val WIDTH = "width"
    const val HEIGHT = "height"
    const val X = "x"
    const val Y = "y"
    const val SORT = "sort"

    private const val DB_TABLE = "player"//表名

    /**
     * 新增一条数据
     */
    fun insert(context: Context, item: PlayerModel?, isTransaction: Boolean = true) : Long{
        if (null == item) {
            return -1L
        }
        val db = DbHelper.getWritableDatabase(context)
        var id: Long = -1
        if (isTransaction) db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ID, item.playerId)
            values.put(WIDTH, item.width)
            values.put(HEIGHT, item.height)
            values.put(X, item.x)
            values.put(Y, item.y)
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
    fun update(context: Context, item: PlayerModel, isTransaction: Boolean = true): Int {
        val db = DbHelper.getWritableDatabase(context)
        val count: Int
        try {
            if (isTransaction) db.beginTransaction()
            val values = ContentValues()
            values.put(ID, item.playerId)
            values.put(WIDTH, item.width)
            values.put(HEIGHT, item.height)
            values.put(X, item.x)
            values.put(Y, item.y)
            values.put(SORT, item.sort)
            count = db.update(DB_TABLE, values, "$ID = ?", arrayOf(item.playerId))
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
    fun get(context: Context, itemId: String, autoCloseDb: Boolean = true): PlayerModel? {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "$ID = ?",
                arrayOf(itemId), null, null, null)
        var item: PlayerModel? = null
        if (cursor != null) {
            if (cursor.moveToFirst()){
                item = PlayerModel(
                        playerId = cursor.getString(cursor.getColumnIndex(ID)),
                        width = cursor.getInt(cursor.getColumnIndex(WIDTH)),
                        height = cursor.getInt(cursor.getColumnIndex(HEIGHT)),
                        x = cursor.getFloat(cursor.getColumnIndex(X)),
                        y = cursor.getFloat(cursor.getColumnIndex(Y)),
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
    fun getAll(context: Context, autoCloseDb: Boolean = true): MutableList<PlayerModel> {
        val db = DbHelper.getReadableDatabase(context)
        val cursor = db.query(DB_TABLE, null, "",
                arrayOf(), null, null, SORT)
        var items : MutableList<PlayerModel> = mutableListOf()
        if (cursor != null) {
            while (cursor.moveToNext()){
                val item = PlayerModel(
                        playerId = cursor.getString(cursor.getColumnIndex(ID)),
                        width = cursor.getInt(cursor.getColumnIndex(WIDTH)),
                        height = cursor.getInt(cursor.getColumnIndex(HEIGHT)),
                        x = cursor.getFloat(cursor.getColumnIndex(X)),
                        y = cursor.getFloat(cursor.getColumnIndex(Y)),
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