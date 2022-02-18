package com.lsinfo.maltose.utils

import android.content.Context
import android.os.Handler
import com.lsinfo.maltose.Config
import com.lsinfo.wsmanager.WsManager
import com.lsinfo.wsmanager.listener.WsStatusListener
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by G on 2018-04-04.
 */
object WsConnector {
    private var wsManager : WsManager? = null

    //private const val WS_HOST = "ws://192.168.0.7:49469/ws"
    //private const val HEART_BEAT_DELAY_MILLIS = 60000L

    fun startDefaultConnect(context: Context, uri: String, wsStatusListener: WsStatusListener): WsManager?  {
        if (wsManager != null) {
            wsManager?.stopConnect()
            wsManager = null
        }
        wsManager = WsManager.Builder(context)
                .client(
                        OkHttpClient().newBuilder()
                                .pingInterval(15, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(true)
                                .build())
                .needReconnect(true)
                .wsUrl(Config.WS_HOST + uri)
                .build()
        wsManager?.setWsStatusListener(wsStatusListener)
        wsManager?.startConnect()
        return wsManager
    }

    fun reconnect(){
        if (wsManager == null) {
            return
        }
        wsManager!!.stopConnect()
        wsManager!!.startConnect()
    }

    class HeartBeatRunnable(val context: Context) : Runnable {
        lateinit var handler: Handler

        override fun run() {
            handler = Handler()
            try {
                if(wsManager != null){
                    wsManager?.sendMessage("/n")
                }
            } catch (e: Exception) {
                LoggerHandler.crashLog.e(e)
            } finally {
                handler.postDelayed(this, Config.HEART_BEAT_DELAY_MILLIS)
            }
        }

        fun stop(){
            handler.removeCallbacks(this)
        }
    }
}