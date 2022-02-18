package com.lsinfo.maltose.service

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import com.lsinfo.maltose.bean.WsMessageTypeEnum
import com.lsinfo.maltose.db.AlarmDbManager
import com.lsinfo.maltose.model.InstructionModel
import com.lsinfo.maltose.observer.AppPathFileObserver
import com.lsinfo.maltose.runnable.AsyncInstructionNotifyRunnable
import com.lsinfo.maltose.runnable.SyncOperationDictionaryRunnable
import com.lsinfo.maltose.runnable.WsDaemonRunnable
import com.lsinfo.maltose.ui.PlayerActivity
import com.lsinfo.maltose.utils.*
import com.lsinfo.wsmanager.WsManager
import com.lsinfo.wsmanager.listener.WsStatusListener
import okhttp3.Response
import okio.ByteString
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*


/**
 * Created by G on 2018-03-13.
 */
class InstructionService: Service() {
    private val tag = "InstructionService"
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val myBinder = MyBinder()
    private var wsManager : WsManager? = null
    private lateinit var asyncInstructionNotifyRunnable: AsyncInstructionNotifyRunnable
    private lateinit var syncOperationDictionaryRunnable: SyncOperationDictionaryRunnable
    private lateinit var wsDaemonRunnable: WsDaemonRunnable
    private lateinit var wsHeartBeatRunnable: WsConnector.HeartBeatRunnable
    private lateinit var mFileObserver: AppPathFileObserver
    private var isFirstConnect = true

    private val wsStatusListener = object : WsStatusListener() {
        override fun onOpen(response: Response) {
            LoggerHandler.commLog.i("InstructionService onOpen:response=${response.toString()}")
            wsHeartBeatRunnable.run()
        }

        override fun onMessage(text: String) {
            LoggerHandler.commLog.i("InstructionService onMessage:text=$text")
            var result = ResultBean()
            try {
                val obj = JSONObject(text)
                when(obj["messageType"]){
                //字符串类型信息
                    WsMessageTypeEnum.Text.ordinal -> {LoggerHandler.runLog.i(obj["data"].toString())}

                //操作指令类型信息
                    WsMessageTypeEnum.ClientMethodInvocation.ordinal -> {
                        if (!obj.has("data")) {
                            result.code = ResultCode.WS_MESSAGE_LAUNCHER_NONE
                        }
                        else {
                            val instruction = InstructionModel.loadFromJson(obj["data"].toString(), result)
                            if (instruction != null && result.isSuccess()){
                                result = instruction.execute(App.getInstance())
                                return
                            }
                        }
                        HttpHandler.instructionOriginalNotifyApi(App.getInstance(), text, result)
                    }

                //连接成功类型信息
                    WsMessageTypeEnum.ConnectionEvent.ordinal -> {
                        Config.TOKEN = obj["data"].toString()
                        if (isFirstConnect){
                            isFirstConnect=false
                            startRunnable()
                            HttpHandler.checkUpgrade(App.getInstance())
                            HttpHandler.syncPlayerList(App.getInstance())
                            HttpHandler.syncPlayInfoList(App.getInstance())
                            HttpHandler.syncPlayInfoResourcesList(App.getInstance())
                            AlarmDbManager.getAll(App.getInstance()).forEach {
                                it.startAlarm(App.getInstance())
                            }
                        }
                    }
                }
            }
            catch (e: Exception){
                result.code = ResultCode.WS_MESSAGE_ERROR
                result.exception = e
                HttpHandler.instructionOriginalNotifyApi(App.getInstance(), text, result)
            }
        }

        override fun onMessage(bytes: ByteString) {
            LoggerHandler.commLog.i("InstructionService WsManager-----onMessage:bytes=$bytes")
            //val instructionManager = InstructionHandler(App.getInstance(), bytes)
            //instructionManager.startAlarm()
        }

        override fun onReconnect() {
            LoggerHandler.commLog.i("InstructionService WsManager-----onReconnect")
        }

        override fun onClosing(code: Int, reason: String) {
            LoggerHandler.commLog.i("InstructionService WsManager-----onClosing:code=${code.toString()},reason=$reason")
        }

        override fun onClosed(code: Int, reason: String) {
            LoggerHandler.commLog.i("InstructionService onClosed:code=${code.toString()},reason=$reason")
            //连接关闭时清除心跳定时器
            wsHeartBeatRunnable.stop()
        }

        override fun onFailure(t: Throwable, response: Response?) {
            Config.TOKEN = ""
            LoggerHandler.commLog.w("InstructionService onFailure:response=${response.toString()}", t)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return myBinder
    }

    override fun onCreate() {
        LoggerHandler.runLog.d("InstructionService onCreate")
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(App.getInstance())
        if (!Config.SETTING_IS_LOCAL_PLAY){
            asyncInstructionNotifyRunnable = AsyncInstructionNotifyRunnable(App.getInstance())
            syncOperationDictionaryRunnable = SyncOperationDictionaryRunnable(App.getInstance())
            wsHeartBeatRunnable = WsConnector.HeartBeatRunnable(App.getInstance())
            mFileObserver = AppPathFileObserver(App.getInstance(), Config.APP_DATA_PATH)
            mFileObserver.startWatching() //开始监听
            startDefaultConnect()
            wsDaemonRunnable = WsDaemonRunnable(App.getInstance(), wsManager)
            wsDaemonRunnable.run()
            SecurityUtils.checkAllPlayInfo(App.getInstance())
        }
        startPlayerActivity()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        stopRunnable()
        //关闭连接
        wsManager?.stopConnect()

        //重新启动服务，保持活动
        val localIntent = Intent()
        localIntent.setClass(App.getInstance(), InstructionService::class.java) //销毁时重新启动Service
        startService(localIntent)
    }

    private fun startRunnable(){
        asyncInstructionNotifyRunnable.run()
        syncOperationDictionaryRunnable.run()
    }

    private fun stopRunnable(){
        asyncInstructionNotifyRunnable.handler.removeCallbacks(asyncInstructionNotifyRunnable)
        syncOperationDictionaryRunnable.handler.removeCallbacks(syncOperationDictionaryRunnable)
        wsDaemonRunnable.handler.removeCallbacks(wsDaemonRunnable)
    }

    /***
     * 启动默认WebSocket连接
     */
    private fun startDefaultConnect() {
        wsManager = WsConnector.startDefaultConnect(baseContext, "?deviceId=${URLEncoder.encode(Config.DEVICE_ID, "UTF-8")}", wsStatusListener)
    }

    private fun startPlayerActivity(){
        if (App.mCurrentActivity == null || App.mCurrentActivity !is PlayerActivity){
            val playerIntent = Intent()
            playerIntent.flags = FLAG_ACTIVITY_NEW_TASK
            playerIntent.setClass(this@InstructionService, PlayerActivity::class.java)
            startActivity(playerIntent)
        }
    }

    inner class MyBinder : Binder() {

        val service: InstructionService
            get() = this@InstructionService
    }
}