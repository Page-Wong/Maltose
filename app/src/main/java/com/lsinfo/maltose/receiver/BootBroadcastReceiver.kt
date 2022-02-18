package com.lsinfo.maltose.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import com.lsinfo.maltose.service.InstructionService
import com.lsinfo.maltose.ui.MainActivity
import com.lsinfo.maltose.utils.LoggerHandler

/**
 * Created by G on 2018-03-16.
 */
class BootBroadcastReceiver : BroadcastReceiver() {
    val ACTION = "android.intent.action.BOOT_COMPLETED"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION) {
            val it = Intent(context, MainActivity::class.java)
            it.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
            LoggerHandler.runLog.i("BootBroadcastReceiver 开机自启动APP")

            /*val service = Intent(context, InstructionService::class.java)
            context.startService(service)
            Log.d(ContentValues.TAG, "开机自启动服务")*/
        }
    }
}