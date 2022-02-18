package com.lsinfo.maltose.runnable

import android.content.Context
import android.os.Handler
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.ui.PlayerActivity
import com.lsinfo.maltose.utils.LoggerHandler
import com.yf_jni.watchdog.watchdog_jni
import java.util.*


/**
 * Created by G on 2018-04-03.
 */
class WatchdogRunnable(val context: Context, val watchDog:watchdog_jni) : Runnable {
    lateinit var handler: Handler

    private var firstRun = true

    override fun run() {
        handler = Handler()
        try {
            val fd = watchDog.getfd()
            if (firstRun){
                if(fd > 0) watchDog.EnableWatchdog(fd)
                firstRun = false
            }
            if(fd > 0){
                watchDog.FeedWatchdog(fd)
                watchDog.SetWatchdogTime(fd, 30)
            }
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        finally {
            handler.postDelayed(this, Config.FEED_WATCHDOG_DURATION)
        }
    }
}