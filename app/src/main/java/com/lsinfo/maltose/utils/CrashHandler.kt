package com.lsinfo.maltose.utils

import android.os.Environment.MEDIA_MOUNTED
import android.annotation.SuppressLint
import java.lang.reflect.AccessibleObject.setAccessible
import android.os.Build
import android.R.attr.versionCode
import android.R.attr.versionName
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.lsinfo.maltose.Config
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent.getIntent
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.lsinfo.maltose.ui.MainActivity






/**
 * Created by G on 2018-05-14.
 */
class CrashHandler private constructor(): Thread.UncaughtExceptionHandler {
    val TAG = "CrashHandler"

    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    // 程序的Context对象
    private var mContext: Context? = null
    // 用来存储设备信息和异常信息
    private val infos = HashMap<String, String>()

    // 用于格式化日期,作为日志文件名的一部分
    private val formatter = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN)

    private object Holder { val INSTANCE = CrashHandler() }

    companion object {
        val instance: CrashHandler by lazy { Holder.INSTANCE }
    }

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        }
        else{
            /*try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                Log.e(TAG, "error : ", e)
            }*/

            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)

            /*// 重新启动程序，注释上面的退出程序
            val intent = Intent()
            intent.setClass(mContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext!!.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())*/
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext)
        // 保存日志文件
        saveCrashInfo2File(ex)
        return true
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.getPackageManager()
            val pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString()
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "an error occured when collect package info", e)
        }

        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field.get(null).toString()
                Log.d(TAG, field.name + " : " + field.get(null))
            } catch (e: Exception) {
                Log.e(TAG, "an error occured when collect crash info", e)
            }

        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfo2File(ex: Throwable) {

        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }

        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause: Throwable? = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            LoggerHandler.crashLog.e(infos.map { "${it.key}=${it.value}\n" } + ex.message)
        } catch (e: Exception) {
            Log.e(TAG, "an error occured while writing file...", e)
        }
    }
}