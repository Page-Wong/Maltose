package com.lsinfo.maltose.runnable

import android.content.Context
import android.os.Handler
import android.util.Log
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.model.PlayInfoModel
import com.lsinfo.maltose.model.PlayInfoStatus
import com.lsinfo.maltose.model.PlayerModel
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.LoggerHandler
import java.util.*

/**
 * Created by G on 2018-04-03.
 */
class LoopPlayRunnable (val context: Context, private val player: PlayerModel) : Runnable {
    private var handler: Handler = Handler()
    var isRunning = false

    override fun run() {
        isRunning = true
        //停止之前的定时器
        handler.removeCallbacks(this)
        var result = ResultBean()
        try {
            if (player.isReady()) {
                var duration = Date()
                result = player.playNext(context)
                LoggerHandler.playLog.i(mapOf<String, String>(
                        Pair("operation","LoopPlayRunnable"),
                        Pair("duration",(Date().time - duration.time).toString()),
                        Pair("result",result.toString()),
                        Pair("playerId", player.playerId),
                        Pair("playInfo", ConvertHelper.gson.toJson(player.playInfo))
                        ))
            }

        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        finally {
            if (!result.isSuccess()){
                handler.postDelayed(this, Config.PLAY_DELAY_MILLIS)
            }
        }
    }

    fun stop(){
        handler.removeCallbacks(this)
        isRunning = false
    }

    fun refreshDuration(){
        val delayMillis = if (player.playInfo != null) player.playInfo!!.duration else Config.PLAY_DELAY_MILLIS
        if (delayMillis != 0L){
            handler.removeCallbacks(this)
            handler.postDelayed(this, delayMillis)
        }
    }
}