package com.lsinfo.maltose.utils

import android.content.Context
import com.lsinfo.maltose.App
import com.lsinfo.maltose.db.PlayerDbManager
import com.lsinfo.maltose.model.PlayerModel
import com.lsinfo.maltose.ui.PlayerActivity

/**
 * Created by G on 2018-04-18.
 */
object PlayerListManager {
    var playerList : MutableList<PlayerModel> = mutableListOf()
        get() {
        if (field.isEmpty())
            field = PlayerDbManager.getAll(App.getInstance())
        return field
    }

    fun refreshPlayerList(){
        /*playerList.forEach {
            it.destroy()
        }
        playerList.clear()
        playerList = PlayerDbManager.getAll(App.getInstance())*/
        var newPlayerList = PlayerDbManager.getAll(App.getInstance())
        playerList.filter { !newPlayerList.map { nit -> nit.playerId }.contains(it.playerId) }.forEach {
            it.destroy()
            playerList.remove(it)
        }
        playerList.forEach {
            var newPlayer = newPlayerList.find { nit -> nit.playerId == it.playerId }
            if (newPlayer != null){
                it.width = newPlayer.width
                it.height = newPlayer.height
                it.x = newPlayer.x
                it.y = newPlayer.y
                it.sort = newPlayer.sort
            }
        }

        newPlayerList.forEach {
            if(!playerList.map { it.playerId }.contains(it.playerId)){
                playerList.add(it)
            }
        }
    }

    fun getPlayer(playerId: String): PlayerModel?{
        return playerList.find { it.playerId == playerId }
    }

    fun startAllLoop(context: Context) {
        if (App.mCurrentActivity == null || App.mCurrentActivity !is PlayerActivity){
            return
        }

        playerList.forEach {
            App.mCurrentActivity!!.runOnUiThread{
                it.startLoop(context)
            }
        }
    }

    fun stopAllLoop(context: Context) {
        playerList.forEach {
            App.mCurrentActivity!!.runOnUiThread{
                it.stopLoop(context)
            }
        }
    }
}