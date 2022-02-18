package com.lsinfo.maltose

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import android.app.Activity
import android.os.Build
import android.os.Bundle
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.lsinfo.maltose.utils.CrashHandler
import com.elvishew.xlog.interceptor.BlacklistTagsFilterInterceptor
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.XLog
import com.lsinfo.maltose.runnable.WatchdogRunnable
import com.lsinfo.maltose.utils.LoggerHandler
import com.yf_jni.watchdog.watchdog_jni
import java.lang.reflect.AccessibleObject.setAccessible
import java.lang.reflect.Method


/**
 * Created by G on 2018-04-02.
 */
class App : Application() {


    //请求队列
    /**
     * 返回请求队列
     * @return
     */
    lateinit var requestQueue: RequestQueue
    //SharedPreferences,用于存储少量的数据
    private lateinit var _preferences: SharedPreferences
    private lateinit var watchdogRunnable: WatchdogRunnable


    val watchDog = watchdog_jni()

    init {
        super.onCreate()
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        //mDb = Database.getInstance(this);
        Log.v("hello", "APP onCreate~~")

        initXLog()
        val mGecExceptionHandle = CrashHandler.instance
        mGecExceptionHandle.init(applicationContext)

        hookWebView()
        _preferences = getSharedPreferences("App", 0)
        requestQueue = Volley.newRequestQueue(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityStarted(p0: Activity?) {

                }

                override fun onActivityDestroyed(p0: Activity?) {

                }

                override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {

                }

                override fun onActivityStopped(p0: Activity?) {

                }

                override fun onActivityCreated(p0: Activity?, p1: Bundle?) {

                }

                override fun onActivityPaused(p0: Activity?) {

                }

                override fun onActivityResumed (activity: Activity){
                    mCurrentActivity = activity
                }
            });
        }

        if (Config.SETTING_ENABLE_WATCHDOG && !Config.SETTING_IS_LOCAL_PLAY){
            runWatchdog()
        }
    }

    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_UI_HIDDEN && Config.SETTING_ENABLE_WATCHDOG)
            watchdogRunnable.handler.removeCallbacks(watchdogRunnable)
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        if (Config.SETTING_ENABLE_WATCHDOG)
            watchdogRunnable.handler.removeCallbacks(watchdogRunnable)
        super.onTerminate()
    }

    public fun runWatchdog(){
        watchdogRunnable = WatchdogRunnable(applicationContext, watchDog)
        watchdogRunnable.run()
    }

    public fun removeWatchdog(){
        val fd = watchdog_jni().getfd()
        if(fd > 0) watchdog_jni().DisableWatchdog(fd)
    }

    /**
     * 初始化日志管理器
     */
    private fun initXLog(){
        val config = LogConfiguration.Builder()
                .logLevel(if (BuildConfig.DEBUG)
                    LogLevel.ALL             // 指定日志级别，低于该级别的日志将不会被打印，默认为 LogLevel.ALL
                else
                    LogLevel.INFO)
                .build()
        XLog.init(                                                 // 初始化 XLog
                config)
    }

    /**
     * 用于检测返回头中包含的cookie
     * 并且更新本地存储的cookie
     * @param headers
     */
    fun checkSessionCookie(headers: Map<String, String>) {
        if (headers.containsKey(SET_COOKIE_KEY)) {
            var cookie = headers[SET_COOKIE_KEY]
            if (!cookie.isNullOrEmpty() && !cookie!!.contains("saeut")) {
                val splitCookie = cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val splitSessionId = splitCookie[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                cookie = splitSessionId[1]
                val prefEditor = _preferences.edit()
                prefEditor.putString(COOKIE_USERNAME, cookie)
                prefEditor.apply()
            }
        }
    }

    /**
     * 向请求头中加入cookie
     * @param headers
     */
    fun addSessionCookie(headers: MutableMap<String, String>) {
        val sessionId = _preferences.getString(COOKIE_USERNAME, "")
        if (sessionId!!.isNotEmpty()) {
            val builder = StringBuilder()
            builder.append(COOKIE_USERNAME)
            builder.append("=")
            builder.append(sessionId)
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ")
                builder.append(headers[COOKIE_KEY])
            }
            headers[COOKIE_KEY] = builder.toString()
        }
    }

    companion object {

        internal var instance: App? = null

        var mCurrentActivity: Activity? = null

        //关于cookie的关键字
        private const val SET_COOKIE_KEY = "Set-Cookie"
        private const val COOKIE_KEY = "Cookie"
        private const val COOKIE_USERNAME = ".AspNet.ApplicationCookie"

        fun getInstance(): App {
            if (instance == null) {
                instance = App()
            }
            return instance!!
        }
    }

    fun hookWebView() {
        val sdkInt = Build.VERSION.SDK_INT
        try {
            val factoryClass = Class.forName("android.webkit.WebViewFactory")
            val field = factoryClass.getDeclaredField("sProviderInstance")
            field.isAccessible = true
            var sProviderInstance = field.get(null)
            if (sProviderInstance != null) {
                LoggerHandler.runLog.i("sProviderInstance isn't null")
                return
            }

            val getProviderClassMethod: Method
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass")
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass")
            } else {
                LoggerHandler.runLog.i("Don't need to Hook WebView")
                return
            }
            getProviderClassMethod.setAccessible(true)
            val factoryProviderClass = getProviderClassMethod.invoke(factoryClass) as Class<*>
            val delegateClass = Class.forName("android.webkit.WebViewDelegate")
            val delegateConstructor = delegateClass.getDeclaredConstructor()
            delegateConstructor.isAccessible = true
            if (sdkInt < 26) {//低于Android O版本
                val providerConstructor = factoryProviderClass.getConstructor(delegateClass)
                if (providerConstructor != null) {
                    providerConstructor.isAccessible = true
                    sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance())
                }
            } else {
                val chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD")
                chromiumMethodName.isAccessible = true
                var chromiumMethodNameStr: String? = chromiumMethodName.get(null) as String
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create"
                }
                val staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass)
                if (staticFactory != null) {
                    sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance())
                }
            }

            if (sProviderInstance != null) {
                field.set("sProviderInstance", sProviderInstance)
                LoggerHandler.runLog.i("Hook success!")
            } else {
                LoggerHandler.runLog.i("Hook failed!")
            }
        } catch (e: Throwable) {
            LoggerHandler.runLog.w(e)
        }

    }
}
