package com.lsinfo.maltose.utils

import android.widget.Toast
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.Logger
import com.elvishew.xlog.XLog
import com.elvishew.xlog.interceptor.Interceptor
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import com.lsinfo.maltose.App
import com.lsinfo.maltose.BuildConfig
import com.lsinfo.maltose.Config
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import com.elvishew.xlog.printer.AndroidPrinter



/**
 * Created by G on 2018-05-14.
 */
object LoggerHandler {
    val crashLog: Logger by lazy {
        createLogger("Crash")
    }

    val playLog: Logger by lazy {
        createLogger("Play")
    }

    val fileLog: Logger by lazy {
        createLogger("File")
    }

    val commLog: Logger by lazy {
        createLogger("Comm")
    }

    val alarmLog: Logger by lazy {
        createLogger("Alarm")
    }

    val runLog: Logger by lazy {
        createLogger("Run")
    }

    var loggerMap :HashMap<String, Logger> = hashMapOf<String, Logger>()

    private fun createLogger(name: String): Logger{
        if (!loggerMap.containsKey(name)){
            loggerMap[name] =  XLog.tag(name).logLevel(
                if (BuildConfig.DEBUG)
                    LogLevel.ALL             // 指定日志级别，低于该级别的日志将不会被打印，默认为 LogLevel.ALL
                else
                    LogLevel.ALL).t().st(2).b().
                    printers(FilePrinter.Builder("${Config.APP_PATH}/Log/${name}Log/").fileNameGenerator(DateFileNameGenerator()).build(),
                            AndroidPrinter()).
                    addInterceptor(Interceptor {
                        if (it.level == LogLevel.ERROR){
                            //TODO G 测试时关闭反馈
                            if (!BuildConfig.DEBUG) HttpHandler.systemErrorNotify(App.getInstance(), it)
                        }
                        it.msg = "\n${SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).format(Date())} ${it.msg}"
                        it
                }) .build()
        }
        return loggerMap[name]!!
    }

    internal class DateFileNameGenerator : FileNameGenerator {

        override fun isFileNameChangeable(): Boolean {
            return false
        }

        override fun generateFileName(logLevel: Int, timestamp: Long): String {
            return "${SimpleDateFormat(Config.DATE_LONG_TIME_FORMAT_PATTERN).format(Date(timestamp))}-$logLevel"
        }
    }
}