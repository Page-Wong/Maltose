package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.db.PlayerPlayInfoDbManager
import com.lsinfo.maltose.utils.FileUtils
import com.lsinfo.maltose.utils.SecurityUtils
import java.io.File


/**
 * Created by G on 2018-04-09.
 */

class PlayerPlayInfoModel(
        val sort: Int,
        var playInfoId: String,
        var playerId: String
) : IPlayerPlayInfoModel{

    override fun save(context: Context): Boolean {
        return if (PlayerPlayInfoDbManager.get(context, playerId, playInfoId, sort) == null){
            PlayerPlayInfoDbManager.insert(context, this) > 0
        }
        else{
            PlayerPlayInfoDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return PlayerPlayInfoDbManager.delete(context, playerId, playInfoId) > 0
    }
}