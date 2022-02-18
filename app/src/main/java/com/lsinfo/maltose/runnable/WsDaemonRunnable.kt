package com.lsinfo.maltose.runnable

import android.content.Context
import android.os.Handler
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.utils.DeviceSettingUtils
import com.lsinfo.maltose.utils.LoggerHandler
import com.lsinfo.maltose.utils.WsConnector
import com.lsinfo.wsmanager.WsManager
import java.util.*


/**
 * Created by G on 2018-04-03.
 */
class WsDaemonRunnable(val context: Context, private val wsManager : WsManager?) : Runnable {
    lateinit var handler: Handler
    var disconnectTime: Date? = null

    override fun run() {
        handler = Handler()
        try {
            if(wsManager == null || !wsManager.isWsConnected){
//                if (disconnectTime == null) {
//                    disconnectTime = Date()
//                }
//                if ((Date().time - disconnectTime!!.time) > 1000 * 60 * 60){
//                    DeviceSettingUtils.reboot()
//                }
                WsConnector.reconnect()
            }
            else {
                disconnectTime = null
            }
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        finally {
            handler.postDelayed(this, Config.WEBSOCKET_DAEMON_DURATION)
        }
    }
}