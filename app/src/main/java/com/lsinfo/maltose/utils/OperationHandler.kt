package com.lsinfo.maltose.utils

import android.content.Context
import android.util.Log
import com.example.yf_rk3288_api.YF_RK3288_API_Manager
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import com.lsinfo.maltose.model.PlayerMode
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by G on 2018-03-20.
 */
class OperationHandler(val context: Context) {
    fun test(arg: MutableMap<String, String?>): ResultBean {
        Log.i("WsService", "test OperationHandler :${arg["arg"]}")
        return ResultBean(
                //对于异步操作，如发送服务器请求，返回状态为RUNNING
                code = ResultCode.SUCCESS
        )
    }

//region 数据同步

    /**
     * 同步播放器列表
     */
    fun syncPlayerList(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.syncPlayerList(context, arg)
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 同步节目列表
     */
    fun syncPlayInfoList(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.syncPlayInfoList(context, arg)
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 同步节目资源列表
     */
    fun syncPlayInfoResourcesList(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.syncPlayInfoResourcesList(context, arg)
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 同步操作字典
     */
    fun syncOperationDictionary(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.syncOperationDictionary(context, arg)
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 同步定时器
     */
    fun syncAlarm(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.syncAlarm(context, arg)
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 检查APP更新版本
     */
    fun syncAppVersion(arg: MutableMap<String, String?>): ResultBean {
        HttpHandler.checkUpgrade(context, arg)
        return ResultBean(ResultCode.RUNNING)

    }
//endregion

//region 播放控制
    /**
     * 指定播放器指定节目立刻播放
     * 参数：{playerId="abcd1234", playInfoId="efgh56789"}
     */
    fun play(arg: MutableMap<String, String?>): ResultBean {
         if (arg.contains("playerId") && arg.contains("playInfoId")){
            var result = ResultBean(ResultCode.SUCCESS)
            var player = PlayerListManager.getPlayer(arg["playerId"].toString())

            if (player == null) {
                result.code = ResultCode.PLAYER_NONE
                return result
            }
             val playInfo = player.getPlayInfoList(context).findLast { it.playInfoId == arg["playInfoId"].toString() }
             if (playInfo == null){
                 result.code = ResultCode.PLAY_INFO_NONE

             }
             else if (!playInfo.isValid()){
                 result.code = ResultCode.PLAY_INFO_INVALID
             }
             else if (!playInfo.isLauncherExists()){
                 result.code = ResultCode.PLAY_INFO_LAUNCHER_NONE
             }
             else {
                 result = player.play(context, playInfo)
             }
             if (!result.isSuccess()) {
                 player.skip(context, result)
             }
            return result
        }
        else {
             return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 指定播放器播放下一个节目
     * 参数：{playerId="abcd1234"}
     */
    fun playNext(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("playerId")){
            var result = ResultBean(ResultCode.SUCCESS)
            var player = PlayerListManager.getPlayer(arg["playerId"].toString())

            if (player == null) {
                result.code = ResultCode.PLAYER_NONE
                return result
            }

            return player.playNext(context)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 指定播放器设置为列表节目循环
     * 参数：{playerId="abcd1234"}
     */
    fun setLoopPlayMode(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("playerId")){
            var result = ResultBean(ResultCode.SUCCESS)
            var player = PlayerListManager.getPlayer(arg["playerId"].toString())

            if (player == null) {
                result.code = ResultCode.PLAYER_NONE
                return result
            }
            player.mode = PlayerMode.LOOP
            return result
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 指定播放器设置为单节目循环
     * 参数：{playerId="abcd1234"}
     */
    fun setSinglePlayMode(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("playerId")){
            var result = ResultBean(ResultCode.SUCCESS)
            var player = PlayerListManager.getPlayer(arg["playerId"].toString())

            if (player == null) {
                result.code = ResultCode.PLAYER_NONE
                return result
            }
            player.mode = PlayerMode.SINGLE
            return result
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 指定播放器停止播放
     * 参数：{playerId="abcd1234"}
     */
    fun stopPlay(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("playerId")){
            var result = ResultBean(ResultCode.SUCCESS)
            var player = PlayerListManager.getPlayer(arg["playerId"].toString())

            if (player == null) {
                result.code = ResultCode.PLAYER_NONE
                return result
            }
            return player.stop(context, ResultBean(ResultCode.OPERATION_HANDLER_STOP_PLAY))
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }
//endregion

//region 获取设备信息
    /**
     * 获取APP版本号
     */
    fun getAppVersion(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postAppVersion(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 获取系统信息
     */
    fun getSystemInfo(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postSystemInfo(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 获取系统播放信息
     */
    fun getSystemPlayInfo(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postSystemPlayInfo(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 获取SDCard信息
     */
    fun getSDCardInfo(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postSDCardInfo(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 获取内存信息
     */
    fun getRamInfo(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postRamInfo(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 获取网络状况
     */
    fun getNetworkInfo(arg: MutableMap<String, String?>): ResultBean {
        val result = HttpHandler.postNetworkInfo(context, arg)
        return if (result.code == ResultCode.SUCCESS){
            ResultBean(ResultCode.RUNNING)
        }
        else {
            result
        }
    }

    /**
     * 上传日志
     */
    fun getLog(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("logType")){
            val name = arg["logType"].toString()
            val srcFilePath = "${Config.APP_PATH}/Log/${name}Log"
            val fileList = File(srcFilePath).listFiles()
            val zipFilePath = "${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/${Date().time.toString()}.zip"
            var zipFile = File(zipFilePath)
            if (!zipFile.exists())
                zipFile.createNewFile()
            ZipUtils.zipFiles(fileList.asList(), zipFile);
            //FileUtils.zipFolder(srcFilePath,zipFilePath)
            zipFile = File(zipFilePath)
            UploadFileHelper.asyncParamsUploadFile(Config.LOG_UPLOAD, zipFile, ConvertHelper.mapStringString2mapStringAny(arg), uploadCallBackListener = object:UploadFileHelper.UploadCallBackListener{

                override fun onSuccess(json: JSONObject) {
                    fileList.forEach {
                        it.delete()
                    }
                    super.onSuccess(json)
                }

                override fun onCallBack(result: ResultBean) {
                    zipFile.delete()
                    if (arg.contains("instructionId") && arg.contains("operationId"))
                        HttpHandler.operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                }

            })
            return ResultBean(ResultCode.RUNNING)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }
//endregion

    //region 设备信息设置

    /**
     * 设置音量
     * 参数：{volume="13"}
     */
    fun setVolume(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("volume")){
            if (DeviceSettingUtils.setVolume(context, arg["volume"]!!.toInt())){
                return ResultBean(ResultCode.SUCCESS)
            }
            return ResultBean(ResultCode.OPERATE_ERROR)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置屏幕亮度(0--255)
     * 参数：{brightness="255"}
     */
    fun setScreenBrightness(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("brightness")){
            if (DeviceSettingUtils.setScreenBrightness(context, arg["brightness"]!!.toInt())){
                return ResultBean(ResultCode.SUCCESS)
            }
            return ResultBean(ResultCode.OPERATE_ERROR)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     * 参数：{mode="1"}
     */
    fun setScreenBrightnessMode(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("mode")){
            if (DeviceSettingUtils.setScreenBrightnessMode(context, arg["mode"]!!.toInt())){
                return ResultBean(ResultCode.SUCCESS)
            }
            return ResultBean(ResultCode.OPERATE_ERROR)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置屏幕方向(0,90,180,270)
     * 参数：{orientation="90"}
     */
    fun setScreenOrientation(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("orientation")){
            if (DeviceSettingUtils.setScreenOrientation(arg["orientation"]!!.toInt())){
                return ResultBean(ResultCode.SUCCESS)
            }
            return ResultBean(ResultCode.OPERATE_ERROR)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 人体反应的响应时间
     * 参数：{time="1000"}
     */
    fun setHumanSensor(arg: MutableMap<String, String?>): ResultBean {
        return if (arg.contains("time")){
            DeviceSettingUtils.setHumanSensor(arg["time"]!!.toInt())
            ResultBean(ResultCode.SUCCESS)
        }
        else {
            ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置状态栏显示/隐藏，占位，不重启
     * 参数：{enable="true"}
     */
    fun setStatusBarVisibility(arg: MutableMap<String, String?>): ResultBean {
        return if (arg.contains("enable")){
            DeviceSettingUtils.setStatusBarVisibility(arg["enable"]!!.toBoolean())
            ResultBean(ResultCode.SUCCESS)
        }
        else {
            ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置状态栏显示/隐藏，不占位，重启
     * 参数：{enable="true"}
     */
    fun setStatusBarDisplay(arg: MutableMap<String, String?>): ResultBean {
        return if (arg.contains("enable")){
            DeviceSettingUtils.setStatusBarDisplay(arg["enable"]!!.toBoolean())
            ResultBean(ResultCode.SUCCESS)
        }
        else {
            ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 设置虚拟按键显示/隐藏
     * 参数：{enable="true"}
     */
    fun setNavigationBarVisibility(arg: MutableMap<String, String?>): ResultBean {
        return if (arg.contains("enable")){
            DeviceSettingUtils.setNavigationBarVisibility(arg["enable"]!!.toBoolean())
            ResultBean(ResultCode.SUCCESS)
        }
        else {
            ResultBean(ResultCode.PARAMS_INVALID)
        }
    }
    //endregion

    //region 设备控制
    /**
     * 打开WIFI
     */
    fun openWifi(arg: MutableMap<String, String?>): ResultBean {
        if (DeviceSettingUtils.openWifi(context)){
            return ResultBean(ResultCode.SUCCESS)
        }
        return ResultBean(ResultCode.OPERATE_ERROR)

    }

    /**
     * 关闭WIFI
     */
    fun closeWifi(arg: MutableMap<String, String?>): ResultBean {
        if (DeviceSettingUtils.closeWifi(context)){
            return ResultBean(ResultCode.SUCCESS)
        }
        return ResultBean(ResultCode.OPERATE_ERROR)

    }

    /**
     * 连接指定WIFI，1.无密码连接 2.WEP加密连接 3.WPA加密连接
     * 参数：{ssid="wifiName", password="123456", type="3"}
     */
    fun connectWifi(arg: MutableMap<String, String?>): ResultBean {
        if (arg.contains("ssid") && arg.contains("password") && arg.contains("type")){
            if (DeviceSettingUtils.connectWifi(context,arg["ssid"]!!, arg["password"]!!,arg["type"]!!.toInt())){
                return ResultBean(ResultCode.SUCCESS)
            }
            return ResultBean(ResultCode.OPERATE_ERROR)
        }
        else {
            return ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 截屏
     */
    fun screenshot(arg: MutableMap<String, String?>): ResultBean {
        val file = DeviceSettingUtils.screenshot() ?: return ResultBean(ResultCode.SCREENSHOT_NONE)
        //val file = File("/sdcard/1.png");
        ImageUtils.ratioAndGenThumb(file.path, "${Config.APP_SCREENSHOT_PATH}/${file.name}_ratio.png", 270F, 480F, true)
        val img = File("${Config.APP_SCREENSHOT_PATH}/${file.name}_ratio.png")
        UploadFileHelper.asyncParamsUploadFile(Config.SCREENSHOT_UPLOAD, img, ConvertHelper.mapStringString2mapStringAny(arg), uploadCallBackListener = object:UploadFileHelper.UploadCallBackListener{

            override fun onCallBack(result: ResultBean) {
                if (file.exists()) {
                    file.delete()
                }
                if (img.exists()) {
                    img.delete()
                }
                if (arg.contains("instructionId") && arg.contains("operationId"))
                    HttpHandler.operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
            }
        })
        return ResultBean(ResultCode.RUNNING)
    }

    /**
     * 格式化资源文件夹
     */
    fun formatData(arg: MutableMap<String, String?>): ResultBean {
        if (DeviceSettingUtils.formatData()){
            return ResultBean(ResultCode.SUCCESS)
        }
        return ResultBean(ResultCode.OPERATE_ERROR)

    }

    /**
     * 定时开机关机
     * 参数：{onTime="2018-01-02 17:30:00", offTime="2018-01-01 17:30:00"}
     */
    fun scheduledOnOff(arg: MutableMap<String, String?>): ResultBean {
        return if (arg.contains("onTime") && arg.contains("offTime")){
            try {
                val onTime = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).parse(arg["onTime"]!!.toString())
                val offTime = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).parse(arg["offTime"]!!.toString())
                DeviceSettingUtils.scheduledOnOff(onTime, offTime)
                ResultBean(ResultCode.SUCCESS)
            }
            catch (e:Exception){
                LoggerHandler.crashLog.e(e)
                ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
            }
        }
        else {
            ResultBean(ResultCode.PARAMS_INVALID)
        }
    }

    /**
     * 打开屏幕，唤醒
     */
    fun turnOnScreen(arg: MutableMap<String, String?>): ResultBean {
        return try {
                DeviceSettingUtils.turnOnScreen()
                ResultBean(ResultCode.SUCCESS)
            }
            catch (e:Exception){
                LoggerHandler.crashLog.e(e)
                ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
            }
    }

    /**
     * 关闭屏幕，待机
     */
    fun turnOffScreen(arg: MutableMap<String, String?>): ResultBean {
        return try {
            DeviceSettingUtils.turnOffScreen()
            ResultBean(ResultCode.SUCCESS)
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
            ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
        }
    }

    /**
     * 重启
     */
    fun reboot(arg: MutableMap<String, String?>): ResultBean {
        return try {
            DeviceSettingUtils.reboot()
            ResultBean(ResultCode.SUCCESS)
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
            ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
        }
    }

    /**
     * 开启远程调试
     */
    fun startAdbd(arg: MutableMap<String, String?>): ResultBean {
        return try {
            var msg = YF_RK3288_API_Manager(App.getInstance()).adbcommand("setprop service.adb.tcp.port 20066"+ "\n"+"stop adbd"+ "\n"+"start adbd")
            ResultBean(ResultCode.SUCCESS, exception = Exception(msg))
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
            ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
        }
    }

    /**
     * 关闭远程调试
     */
    fun stopAdbd(arg: MutableMap<String, String?>): ResultBean {
        return try {
            YF_RK3288_API_Manager(App.getInstance()).adbcommand("stop adbd")
            ResultBean(ResultCode.SUCCESS)
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
            ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
        }
    }

    /**
     * 开启/关闭看门狗
     */
    fun enableWatchdog(arg: MutableMap<String, String?>): ResultBean {
        return try {
            return if (arg.contains("enable")){
                Config.SETTING_ENABLE_WATCHDOG = arg["enable"]!!.toBoolean()
                if (Config.SETTING_ENABLE_WATCHDOG){
                    App.getInstance().runWatchdog()
                } else{
                    App.getInstance().removeWatchdog()
                }
                ResultBean(ResultCode.SUCCESS)
            } else {
                ResultBean(ResultCode.PARAMS_INVALID)
            }
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
            ResultBean(code = ResultCode.OPERATE_ERROR,exception= e)
        }
    }
    //endregion
}