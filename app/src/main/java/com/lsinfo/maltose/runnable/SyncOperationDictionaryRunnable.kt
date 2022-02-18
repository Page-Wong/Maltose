package com.lsinfo.maltose.runnable

import android.content.Context
import android.os.Handler
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.utils.HttpHandler
import com.lsinfo.maltose.utils.LoggerHandler

/**
 * Created by G on 2018-04-03.
 */
class SyncOperationDictionaryRunnable(val context: Context) : Runnable {
    lateinit var handler: Handler

    override fun run() {
        handler = Handler()
        try {
            if (Config.OPERATION_DICTIONARY_VERSION.isEmpty()){
                HttpHandler.syncOperationDictionary(context)
                handler.postDelayed(this, Config.SYNC_OPERATION_DICTIONARY_DURATION)
            }
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
            handler.postDelayed(this, Config.SYNC_OPERATION_DICTIONARY_DURATION)
        }
    }
}