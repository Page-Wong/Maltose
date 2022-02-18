package com.lsinfo.maltose.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import com.example.yf_rk3288_api.YF_RK3288_API_Manager
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by G on 2018-04-23.
 */
object DeviceSettingUtils {

    private val yfapi = YF_RK3288_API_Manager(App.getInstance())
    /**
     * 打包获取系统信息
     */
    fun getSystemInfo(context: Context):MutableMap<String, String?>{
        var arg: MutableMap<String, String?> = mutableMapOf()
        arg["systemVersion"] = DeviceSettingUtils.getSystemVersion()
        arg["systemModel"] = DeviceSettingUtils.getSystemModel()
        arg["deviceBrand"] = DeviceSettingUtils.getDeviceBrand()
        arg["cpuName"] = DeviceSettingUtils.getCpuName()
        arg["totalRam"] = DeviceSettingUtils.getTotalRam().toString()
        arg["sdCardTotalMemory"] = DeviceSettingUtils.getSDCardTotalMemory().toString()
        arg["macAddress"] = DeviceSettingUtils.getMacAddress(context)
        arg["screenResolutionWidth"] = DeviceSettingUtils.getScreenResolution(context)[0].toString()
        arg["screenResolutionHeight"] = DeviceSettingUtils.getScreenResolution(context)[1].toString()
        arg["screenOrientation"] = DeviceSettingUtils.getScreenOrientation().toString()
        arg["screenBrightnessMode"] = DeviceSettingUtils.getScreenBrightnessMode(context).toString()
        arg["screenBrightness"] = DeviceSettingUtils.getScreenBrightness(context).toString()
        arg["volume"] = DeviceSettingUtils.getVolume(context).toString()
        arg["systemMaxVolume"] = DeviceSettingUtils.getSystemMaxVolume(context).toString()
        return arg
    }

    /**
     * 获取版本名称
     */
    fun getVersionName(context: Context): String? {
        return try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            info.versionName
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
            null
        }
    }

    /**
     * 获取版本号
     */
    fun getVersionCode(context: Context): Int? {
        return try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            info.versionCode
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
            null
        }
    }

    /**
     * 获取包名
     */
    fun getPackageName(context: Context): String? {
        return try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            info.packageName
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
            null
        }
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    fun getSystemVersion(): String {
        return android.os.Build.VERSION.RELEASE
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    fun getSystemModel(): String {
        return android.os.Build.MODEL
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    fun getDeviceBrand(): String {
        return android.os.Build.BRAND
    }

    /**
     * 获取CPU型号
     */
    fun getCpuName(): String? {
        try {
            val fr = FileReader("/proc/cpuinfo")
            val br = BufferedReader(fr)
            val text = br.readLine()
            val array = text.split(":\\s+".toRegex(), 2).toTypedArray()
            for (i in array.indices) {
            }
            return array[1]
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }

        return null
    }

    /**
     * RAM内存大小
     */
    fun getTotalRam(): String {
        return yfapi.yfgetRAMSize()
        /*val str1 = "/proc/meminfo"// 系统内存信息文件
        val str2: String
        val arrayOfString: Array<String>
        var initial_memory: Long = 0

        try {
            val localFileReader = FileReader(str1)
            val localBufferedReader = BufferedReader(
                    localFileReader, 8192)
            str2 = localBufferedReader.readLine()// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (num in arrayOfString) {
                Log.i(str2, num + "\t")
            }

            initial_memory = (Integer.valueOf(arrayOfString[1])!!.toInt() * 1024).toLong()// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close()

        } catch (e: IOException) {
        }
        return initial_memory / (1024 * 1024)*/
    }

    /**
     * 获取当前剩余内存(ram)大小，单位MB
     */
    fun getAvailableRam(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.availMem / (1024 * 1024)
    }

    /**
     * sdCard 总大小
     */
    fun getSDCardTotalMemory(): String {
        return yfapi.yfgetInternalStorageMemory()

        /*val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.getPath())
            val bSize = sf.blockSize.toLong()
            val bCount = sf.blockCount.toLong()

            return bSize * bCount//总大小
        }
        return null*/
    }

    /**
     * sdCard 可用大小
     */
    fun getSDCardAvailableMemory(): String {
        return yfapi.yfgetAvailableInternalMemorySize()

        /*val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.getPath())
            val bSize = sf.blockSize.toLong()
            val availBlocks = sf.availableBlocks.toLong()
            return bSize * availBlocks//可用大小
        }
        return null*/
    }

    /**
     * 获取MAC地址
     */
    fun getMacAddress(context: Context): String? {
        return yfapi.yfgetEthMacAddress()

        /*val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.macAddress != null) {
            return wifiInfo.macAddress
        } else {
            return null
        }*/
    }

    /**
     * 获取屏幕分辨率，1.宽度，2.高度
     */
    fun getScreenResolution(context: Context): Array<Int> {
        /*val dm = context.resources.displayMetrics
        return arrayOf(dm.widthPixels,dm.heightPixels+getNavigationBarHeight(context))*/

        return arrayOf(yfapi.yfgetScreenWidth(), yfapi.yfgetScreenHeight())
    }

    /**
     * 获取虚拟按键的高度
     */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val res = context.resources
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }

        return result
    }

    /**
     * 设置虚拟按键显示/隐藏
     */
    fun setNavigationBarVisibility(enable: Boolean){
        yfapi.yfsetNavigationBarVisibility(enable);

    }

    /**
     * 设置状态栏显示/隐藏，不占位，重启
     */
    fun setStatusBarDisplay(enable: Boolean){
        yfapi.yfsetStatusBarDisplay(enable)
    }

    /**
     * 设置状态栏显示/隐藏，占位，不重启
     */
    fun setStatusBarVisibility(enable: Boolean){
        yfapi.yfsetStatusBarVisibility(enable)
    }

    /**
     * 判断是否有网络连接
     */
    fun isNetworkConnected(context: Context): Boolean {
        val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = mConnectivityManager.activeNetworkInfo
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable
        }
        return false
    }

    /**
     * 判断WIFI网络是否可用
     */
    fun isWifiConnected(context: Context): Boolean {

        val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return mConnectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * 判断MOBILE网络是否可用
     */
    fun isMobileConnected(context: Context): Boolean {

        val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return mConnectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * 获取当前网络连接的类型信息  WIFI:无线网络 2G/3G/4G: 分别表示2G/3G/4G移动数据网络 ETHERNET:表示以太网
     * -1：没有网络 1：WIFI网络 2：wap网络 3：net网络
     */
    fun getConnectedType(context: Context): String {
        return yfapi.yfgetCurrentNetType()

        /*val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = mConnectivityManager.activeNetworkInfo
        if (mNetworkInfo != null && mNetworkInfo.isAvailable) {
            return mNetworkInfo.type
        }

        return -1*/
    }

    /**
     * 打开WIFI
     */
    fun openWifi(context: Context): Boolean {
        try {
            var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }
            return true
        }
        catch (e: Exception){
            LoggerHandler.crashLog.e(e)
        }
        return false
    }

    /**
     * 关闭WIFI
     */
    fun closeWifi(context: Context): Boolean {
        try {
            var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = false
            }
            return true
        }
        catch (e: Exception){
            LoggerHandler.crashLog.e(e)
        }
        return false
    }

    /**
     * 获取附近 WIFI 列表
     */
    fun getWifiList(context: Context): List<ScanResult>? {
        var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.scanResults
    }

    /**
     * 获取目前连接的 WIFI 信息
     */
    fun getWifiInfo(context: Context): WifiInfo? {
        if (!isWifiConnected(context)){
            return null
        }
        var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo
    }

    /**
     * 连接指定的wifi，type：1.无密码连接 2.WEP加密连接 3.WPA加密连接
     */
    fun connectWifi(context: Context, ssid: String, password: String, type: Int): Boolean{
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + ssid + "\""

        if (type === 1)
        //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = ""
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        }
        if (type === 2)
        //WIFICIPHER_WEP
        {
            config.hiddenSSID = true
            config.wepKeys[0] = "\"" + password + "\""
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        }
        if (type === 3)
        //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }

        var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val netId = wifiManager.addNetwork(config)
        return wifiManager.enableNetwork(netId, true)
    }

    /**
     * 获取屏幕方向
     */
    fun getScreenOrientation(): Int?{
        if (App.mCurrentActivity != null){
            return when(App.mCurrentActivity!!.requestedOrientation){
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> 0
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE  -> 90
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> 180
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 270
                else -> null
            }
        }
        return null
    }

    /**
     * 设置屏幕方向(0,90,180,270)，重启
     */
    fun setScreenOrientation(orientation: Int):Boolean{
        if (orientation < 0) {
            return false
        }
        val newOrientation = when (orientation) {
            in 61..119 -> "90"
            in 120..240 -> "180"
            in 241..299 -> "270"
            else -> "0"
        }
        yfapi. yfsetRotation (newOrientation);
        return true
        /*try {
            if (App.mCurrentActivity != null) {
                var newOrientation = when (orientation) {
                    in 0..60 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    in 61..119 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    in 120..240 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    in 241..299 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    in 300..360 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                App.mCurrentActivity!!.requestedOrientation = newOrientation
            }
            Config.SETTING_SCREEN_ORIENTATION = orientation
            return true
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e.toString())
        }
        return false*/
    }

    /**
     * 获得当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    fun getScreenBrightnessMode(context: Context): Int? {
        try {
            return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        return null
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    fun setScreenBrightnessMode(context: Context, paramInt: Int):Boolean {
        try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt)
            Config.SETTING_SCREEN_BRIGHTNESS_MODE = paramInt
            return true
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        return false
    }

    /**
     * 获得当前屏幕亮度值 0--255
     */
    fun getScreenBrightness(context: Context): Int? {
        try {
            return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
        return null
    }

    /**
     * 设置屏幕亮度
     */
    fun setScreenBrightness(context: Context, brightValue: Int):Boolean {
        try {
            setScreenBrightnessMode(context, 0)
            val uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightValue)
            context.contentResolver.notifyChange(uri, null)
            Config.SETTING_SCREEN_BRIGHTNESS = brightValue
            return true
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
        }
        return false
    }

    /**
     * 获取设备最大音量值
     */
    fun getSystemMaxVolume(context: Context):Int {
        return (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * 获取当前的音量值
     */
    fun getVolume(context: Context): Int {
        return (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * 调整音量，自定义
     */
    fun setVolume(context: Context, num: Int):Boolean {
         try {
            val a = when (num){
                in getSystemMaxVolume(context)..Int.MAX_VALUE -> getSystemMaxVolume(context)
                in 0..Int.MIN_VALUE -> 0
                else -> num
            }
            (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).setStreamVolume(AudioManager.STREAM_MUSIC, a,0)
             Config.SETTING_VOLUME = num
             return true
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
        }

        return false
    }

    /**
     * 截屏
     */
    fun screenshot(): File? {
        val fileName = "${Date().time}.png"
        yfapi.yfTakeScreenshot(Config.APP_SCREENSHOT_PATH, fileName)
        return File("${Config.APP_SCREENSHOT_PATH}/$fileName")

        /*if (!isRooted()) return null
        val path = "${Config.APP_SCREENSHOT_PATH}/${Date().time}.png"
        val cmd = "screencap -p $path"
        try {
            val process = Runtime.getRuntime().exec("su")//不同的设备权限不一样
            val pw = PrintWriter(process.outputStream)
            pw.println(cmd)
            pw.flush()
            pw.println("exit")
            pw.flush()
            try {
                process.waitFor()
            } catch (e: Exception) {
                LoggerHandler.crashLog.e(e.toString())
            }

            pw.close()
            process.destroy()
            val file = File(path)
            return if (file.exists()) file else null
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e.toString())
        }
        return null*/
    }

    /**
     * 是否有ROOT权限
     */
    fun isRooted(): Boolean {
        //检测是否ROOT过
        val stream: DataInputStream
        try {
            return ShellUtils.terminal("ls /data/")
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)

        }
        return false
    }

    /**
     * 打开屏幕，唤醒
     */
    fun turnOnScreen() {
        yfapi.yfSetLCDOn()
    }

    /**
     * 关闭屏幕，待机
     */
    fun turnOffScreen() {
        yfapi.yfSetLCDOff()
    }

    /**
     * 重启
     */
    fun reboot() {
        try {
            //ShellUtils.terminal("reboot")
            yfapi.yfReboot()
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
    }

    /**
     * 关机
     */
    fun shutdown(){
        try {
            //ShellUtils.terminal("reboot -p")
            yfapi.yfShutDown()
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
    }

    /**
     * 定时开关机
     */
    fun scheduledOnOff(onTime: Date, offTime: Date){
        try {
            var shutdownParam : IntArray? = null
            val shutdownTime = Calendar.getInstance()
            shutdownTime.time = offTime
            shutdownParam = intArrayOf(
                    shutdownTime.get(Calendar.YEAR),
                    shutdownTime.get(Calendar.MONTH ) + 1,
                    shutdownTime.get(Calendar.DAY_OF_MONTH),
                    shutdownTime.get(Calendar.HOUR_OF_DAY),
                    shutdownTime.get(Calendar.MINUTE),
                    shutdownTime.get(Calendar.SECOND) )


            val bootTime = Calendar.getInstance()
            bootTime.time = onTime
            var bootParam = intArrayOf(
                    bootTime.get(Calendar.YEAR),
                    bootTime.get(Calendar.MONTH) + 1,
                    bootTime.get(Calendar.DAY_OF_MONTH),
                    bootTime.get(Calendar.HOUR_OF_DAY),
                    bootTime.get(Calendar.MINUTE),
                    bootTime.get(Calendar.SECOND))

            yfapi.yfsetOnOffTime(bootParam, shutdownParam, true)

        }
        catch (e: Exception) {
            LoggerHandler.crashLog.e(e)
        }
    }

    /**
     * 格式化资源文件夹
     */
    fun formatData(): Boolean{
        try {
            val dir = File(Config.APP_DATA_PATH)
            FileUtils.getAllFiles(dir, arrayListOf<File>()).forEach { it.delete() }
            return true
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
        }
        return false
    }

    /**
     * 执行具体的静默安装逻辑，需要手机ROOT。
     * @param fileName
     * 要安装的apk文件名
     * @return 安装成功返回true，安装失败返回false。
     */
    fun installApk(fileName: String): Boolean {
        val watchDog = App.getInstance().watchDog
        watchDog.DisableWatchdog(watchDog.getfd())
        val apkPath = "${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/$fileName"
        val excresult = yfapi.adbcommand("pm install -r $apkPath")
        LoggerHandler.runLog.i("installApk $apkPath excresult:$excresult")
        return true
        /*val file = File("${Config.APP_TEMP_PATH}/fileName")
        if (!file.exists()) return false

        try {
            return ShellUtils.terminal("pm install -r " + file.absolutePath)
        } catch (e: Exception) {
            LoggerHandler.crashLog.e(e.toString())
        }
        return false*/
    }

    /**
     * 人体反应的响应时间
     */
    fun setHumanSensor(time: Int){
        yfapi.yfsetHumanSensor(time)
    }
}