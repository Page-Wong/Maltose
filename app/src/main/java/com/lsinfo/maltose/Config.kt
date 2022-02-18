package com.lsinfo.maltose

import android.content.pm.ActivityInfo
import android.preference.PreferenceManager
import com.lsinfo.maltose.utils.FileUtils
import java.io.File
import java.util.*


/**
 * Created by G on 2018-03-14.
 */
object Config {
//region 设备硬件设置
    /** Preference Keys **/
    const val PREFERENCE_SETTING_SCREEN_ORIENTATION_KEY= "preference_setting_screen_orientation_key"
    const val PREFERENCE_SETTING_SCREEN_BRIGHTNESS_MODE_KEY= "preference_setting_screen_brightness_mode_key"
    const val PREFERENCE_SETTING_SCREEN_BRIGHTNESS_KEY= "preference_setting_screen_brightness_key"
    const val PREFERENCE_SETTING_VOLUME_KEY= "preference_setting_volume_key"
    const val PREFERENCE_SETTING_NEXT_BOOT_TIMEY= "preference_setting_next_boot_timey"
    const val PREFERENCE_SETTING_NEXT_SHUTDOWN_TIME= "preference_setting_next_shutdown_time"
    const val PREFERENCE_SETTING_ENABLE_WATCHDOG= "preference_setting_enable_watchdog"
    const val PREFERENCE_SETTING_IS_LOCAL_PLAY= "preference_setting_is_local_play"

    //屏幕方向
    var SETTING_SCREEN_ORIENTATION : Int = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(PREFERENCE_SETTING_SCREEN_ORIENTATION_KEY, 270)
    //亮度模式
    var SETTING_SCREEN_BRIGHTNESS_MODE : Int = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(PREFERENCE_SETTING_SCREEN_BRIGHTNESS_MODE_KEY, 1)
    //亮度
    var SETTING_SCREEN_BRIGHTNESS : Int = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(PREFERENCE_SETTING_SCREEN_BRIGHTNESS_KEY, ActivityInfo.SCREEN_ORIENTATION_BEHIND)
    //音量
    var SETTING_VOLUME : Int = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getInt(PREFERENCE_SETTING_VOLUME_KEY, 1)
    //下一次开机时间
    var SETTING_NEXT_BOOT_TIME : String = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(PREFERENCE_SETTING_NEXT_BOOT_TIMEY, "")
    //下一次关机时间
    var SETTING_NEXT_SHUTDOWN_TIME : String = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(PREFERENCE_SETTING_NEXT_SHUTDOWN_TIME, "")
    //开启看门狗
    var SETTING_ENABLE_WATCHDOG : Boolean = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean(PREFERENCE_SETTING_ENABLE_WATCHDOG, true)
    //开启看门狗
    var SETTING_IS_LOCAL_PLAY : Boolean = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean(PREFERENCE_SETTING_IS_LOCAL_PLAY, true)
//endregion


//region 设备唯一ID、传输令牌
    /** 注册加密口令**/
    const val REGISTRATION_KEY= ""

    /** Preference Keys **/
    const val PREFERENCE_TOKEN_KEY= "preference_token_key"
    const val PREFERENCE_DEVICE_ID_KEY= "preference_device_id_key"
    const val PREFERENCE_OPERATION_DICTIONARY_VERSION_KEY= "preference_operation_dictionary_version_key"

    val DEVICE_ID:String
    get() { return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(PREFERENCE_DEVICE_ID_KEY, "")}

    var TOKEN : String = ""
    var OPERATION_DICTIONARY_VERSION : String = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(PREFERENCE_OPERATION_DICTIONARY_VERSION_KEY, "")


//endregion

//region 其他设置

    /** 日期时间格式化 **/
    const val DATE_LONG_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH_mm_ss_SSSS"
    const val DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"

    /** WebView读取文件本地路径 **/
    const val WEB_VIEW_BASE_URL = "/"

//endregion
        
//region     HTTP请求Host和TAG
    /** 服务器IP地址*/
    val WS_HOST = "ws://127.0.0.1:8082/ws"
    val WS_REGISTER_HOST = "ws://127.0.0.1:8082/wsregist"
    val HTTP_HOST = "http://127.0.0.1:8082"
    val DOWNLOAD_HOST ="http://127.0.0.1:8082"
    /*val WS_HOST = "ws://192.168.0.7:54418/ws"
    val WS_REGISTER_HOST = "ws://192.168.0.7:54418/wsregist"
    val HTTP_HOST = "http://192.168.0.7:54418"
    val DOWNLOAD_HOST ="http://192.168.0.7:54418"*/

    /** 下载资源文件 */
    val RESOURCES_DOWNLOAD = "$DOWNLOAD_HOST/Instruction/DownloadResources"
    /** 下载安装文件 */
    val APP_DOWNLOAD = "$DOWNLOAD_HOST/Instruction/DownloadApp"

    /** 屏幕截图上传 */
    val SCREENSHOT_UPLOAD = "$HTTP_HOST/Instruction/ScreenshotUpload"
    /** 日志文件上传 */
    val LOG_UPLOAD = "$HTTP_HOST/Instruction/LogUpload"

    /** 系统运行错误上报  */
    val API_SYSTEM_ERROR_NOTIFY = "$HTTP_HOST/Instruction/SystemErrorNotify"
    /** 原始指令反馈地址  */
    val API_INSTRUCTION_ORIGINAL_NOTIFY = "$HTTP_HOST/Instruction/InstructionOriginalNotify"
    /** 默认反馈地址  */
    val API_INSTRUCTION_DEFAULT_NOTIFY = "$HTTP_HOST/Instruction/InstructionDefaultNotify"
    /** 同步节目列表 */
    val API_SYN_PLAY_INFO_LIST = "$HTTP_HOST/Instruction/SyncPlayInfoList"
    /** 同步播放器列表 */
    val API_SYN_PLAYER_LIST = "$HTTP_HOST/Instruction/SyncPlayerList"
    /** 同步节目资源列表 */
    val API_SYN_PLAY_INFO_RESOURCES_LIST = "$HTTP_HOST/Instruction/SyncPlayInfoResourcesList"
    /** 同步操作字典 */
    val API_SYN_OPERATION_DICTIONARY = "$HTTP_HOST/Instruction/SyncOperationDictionary"
    /** 同步定时器列表 */
    val API_SYN_ALARM = "$HTTP_HOST/Instruction/SyncAlarm"
    /** 上传APP版本号 */
    val API_POST_APP_VERSION= "$HTTP_HOST/Instruction/PostAppVersion"
    /** 检查APPA更新 */
    val API_CHECK_UPGRADE= "$HTTP_HOST/Instruction/CheckUpgrade"
    /** 上传APP版本号 */
    val API_POST_SYSTEM_INFO= "$HTTP_HOST/Instruction/PostSystemInfo"
    /** 上传系统播放信息  */
    val API_POST_SYSTEM_PLAY_INFO = "$HTTP_HOST/Instruction/PostSystemPlayInfo"
    /** 上传SDCard信息 */
    val API_POST_SDCARD_INFO= "$HTTP_HOST/Instruction/PostSDCardInfo"
    /** 上传内存信息 */
    val API_POST_RAM_INFO= "$HTTP_HOST/Instruction/PostRamInfo"
    /** 上传网络状况 */
    val API_POST_NETWORK_INFO= "$HTTP_HOST/Instruction/PostNetworkInfo"
    /** APP升级处理结果反馈  */
    val API_UPGRADE_NOTIFY = "$HTTP_HOST/Instruction/UpgradeNotify"
    /** 注册地址处理结果反馈  */
    val API_REGISTRATION_URL_NOTIFY = "$HTTP_HOST/Regist/ReceiveRegisterSucceed"

    /** 系统运行错误上报  */
    val API_TAG_SYSTEM_ERROR_NOTIFY = "systemErrorNotify"
    /** 指令反馈请求标签  */
    const val API_TAG_INSTRUCTION_NOTIFY = "instruction_notify_api"
    /** 注册地址处理结果反馈标签  */
    const val API_TAG_REGISTRATION_URL_NOTIFY = "registration_url_api"
    /** 同步节目列表标签  */
    const val API_TAG_SYN_PLAY_INFO_LIST = "syn_play_info"
    /** 同步播放器列表标签  */
    const val API_TAG_SYN_PLAYER_LIST = "syn_player"
    /** 同步节目资源列表 */
    const val API_TAG_SYN_PLAY_INFO_RESOURCES_LIST = "syn_play_info_resources"
    /** 同步操作字典 */
    const val API_TAG_SYN_OPERATION_DICTIONARY = "syn_operation_dictionary"
    /** 同步定时器列表 */
    const val API_TAG_SYN_ALARM = "syn_operation_alarm"
    /** 上传APP版本号 */
    const val API_TAG_POST_APP_VERSION = "post_app_version"
    /** 上传APP版本号 */
    const val API_TAG_POST_SYSTEM_INFO = "post_system_info"
    /** 上传系统播放信息 */
    const val API_TAG_POST_SYSTEM_PLAY_INFO = "post_system_play_info"
    /** APP升级处理结果反馈 */
    const val API_TAG_UPGRADE_NOTIFY = "upgrade_notify"
    /** 检查APPA更新 */
    const val API_TAG_CHECK_UPGRADE = "check_upgrade"
    /** 上传SDCard信息 */
    const val API_TAG_POST_SDCARD_INFO = "post_sdcard_info"
    /** 上传内存信息 */
    const val API_TAG_POST_RAM_INFO = "post_ram_info"
    /** 上传网络状况 */
    const val API_TAG_POST_NETWORK_INFO = "post_network_info"


//endregion

//region Intent参数设置
    /** Intent传参Data的key  */
    const val EXTRA_DATA = "extra_data"
    val EXTRA_WEBSOCKET_ONTEXTMESSAGE_MESSAGE = "extra_websocket_ontextmessage_message"
    const val EXTRA_WEBSOCKET_ONBYTEMESSAGE_MESSAGE = "extra_websocket_onbytemessage_message"
    const val EXTRA_WEBSOCKET_ONCLOSING_CODE = "extra_websocket_onclosing_code"
    const val EXTRA_WEBSOCKET_ONCLOSING_REASON = "extra_websocket_onclosing_reason"
    const val EXTRA_WEBSOCKET_ONCLOSED_CODE = "extra_websocket_onclosed_code"
    const val EXTRA_WEBSOCKET_ONCLOSED_REASON = "extra_websocket_onclosed_reason"
    const val EXTRA_WEBSOCKET_ONFAILURE_T = "extra_websocket_onfailure_t"

    /** Intent的Action*/
    const val ACTION_WEBSOCKET_ONOPEN = "action_websocket_onopen"
    const val ACTION_WEBSOCKET_ONTEXTMESSAGE = "action_websocket_ontextmessage"
    const val ACTION_WEBSOCKET_ONBYTEMESSAGE = "action_websocket_onbytemessage"
    const val ACTION_WEBSOCKET_ONRECONNECT = "action_websocket_onreconnect"
    const val ACTION_WEBSOCKET_ONCLOSING = "action_websocket_onclosing"
    const val ACTION_WEBSOCKET_ONCLOSED = "action_websocket_onclosed"
    const val ACTION_WEBSOCKET_ONFAILURE = "action_websocket_onfailure"
//endregion

//region 操作指令集
    /** 指令操作方法的Class*/
    const val OPERATION_METHOD_CLASS = "com.lsinfo.maltose.utils.OperationHandler"

//endregion

//region 定时器循环间隔
    /**
     * 异步发送指令执行结果通知时间间隔
     */
    const val ASYNC_INSTRUCTION_NOTIFY_DELAY_MILLIS =  60 * 1000L

    /**
     * 同步操作字典时间间隔
     */
    const val SYNC_OPERATION_DICTIONARY_DURATION = 30 * 1000L

    /**
     * 默认循环播放检测时间间隔
     */
    const val PLAY_DELAY_MILLIS = 1 * 1000L

    /**
     * 节目切换动画持续时间
     */
    const val PAGE_LOAD_ANIMATOR_DURATION = 5000L

    /**
     * WebSocket心跳发送间隔
     */
    const val HEART_BEAT_DELAY_MILLIS = 2 * 1000L

    /**
     * 喂狗时间间隔
     */
    const val FEED_WATCHDOG_DURATION = 5 * 1000L
    /**
     * websocket守护进程时间间隔
     */
    const val WEBSOCKET_DAEMON_DURATION = 60 * 1000L
//endregion

//region 文件设置
    val APP_PATH = buildString {
        append("${FileUtils.getSDPath()}/${App.getInstance().packageName}")
            if (!File(toString()).exists())
                    File(toString()).mkdir()
            toString()
    }

    val APP_DATA_PATH = buildString {
        append("$APP_PATH/Data")
        if (!File(toString()).exists())
                File(toString()).mkdir()
        toString()
    }
    val APP_DATA_FILE_PATH = "file://$APP_DATA_PATH"

    //临时文件夹，主要存放下载资源的压缩文件
    val APP_TEMP_PATH = buildString {
        append("/${App.getInstance().packageName}/Temp")
        if (!File(toString()).exists())
                File(toString()).mkdir()
        toString()
    }

    //截屏文件夹，主要存放截屏的图片
    val APP_SCREENSHOT_PATH = buildString {
        append("$APP_PATH/Screenshot")
        if (!File(toString()).exists())
            File(toString()).mkdir()
        toString()
    }
// endregion

}