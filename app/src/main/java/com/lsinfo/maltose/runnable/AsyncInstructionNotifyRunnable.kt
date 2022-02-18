package com.lsinfo.maltose.runnable

import android.content.Context
import android.os.Handler
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.InstructionDbManager
import com.lsinfo.maltose.utils.HttpHandler
import com.lsinfo.maltose.utils.LoggerHandler

/**
 * Created by G on 2018-04-03.
 */
class AsyncInstructionNotifyRunnable(val context: Context) : Runnable {
    lateinit var handler: Handler

    init {
    }

    override fun run() {
        handler = Handler()
        try {
            val instructions = InstructionDbManager.findComplete(context)
            instructions.forEach { item -> HttpHandler.instructionNotifyApi(context, item) }
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        } finally {
            handler.postDelayed(this, Config.ASYNC_INSTRUCTION_NOTIFY_DELAY_MILLIS)
        }
    }
}