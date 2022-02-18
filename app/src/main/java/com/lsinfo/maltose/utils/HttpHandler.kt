package com.lsinfo.maltose.utils

import android.content.Context
import com.elvishew.xlog.LogItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.*
import com.lsinfo.maltose.bean.registration.RegistrationResultBean
import com.lsinfo.maltose.db.*
import com.lsinfo.maltose.model.*
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder
import java.util.*



/**
 * Created by G on 2018-04-02.
 */
object HttpHandler {
    //region 基础工具
    /**
     * 将POST参数加工，加入令牌和参数签名
     */
    fun processPostParams(map: MutableMap<String, String?> = mutableMapOf()):MutableMap<String, String?>{
        map["token"] = Config.TOKEN
        map["timestamp"] = Date().time.toString()
        //获取排除sign后的数据串
        var dataMap = map.toSortedMap()
        /*dataMap.remove("sign")
        var dataString = ConvertHelper.gson.toJson(dataMap).replace("\\\"","").replace("\"","")
        //获取数据串的md5码
        val md5String = SecurityUtils.md5Encrypt(dataString)
        map["sign"] = URLDecoder.decode(md5String, "UTF-8")*/
        map["sign"] =SecurityUtils.sign(dataMap)
        return map
    }

    /**
     * 将GET参数加工，加入令牌和参数签名
     */
    fun processGetParams(map: MutableMap<String, String?> = mutableMapOf()):String{
        val paramMap =  processPostParams(map)
        var paramString = StringBuffer()

        for ((key, value) in paramMap) {
            paramString.append("&$key=${URLDecoder.decode(value, "UTF-8")}")
        }
        return paramString.toString().replaceFirst("&", "")
    }

    /**
     * 按照指令操作后的处理
     */
    fun operationCallBack(context: Context, instructionId:String, operationId:String, result: ResultBean){
        val operation = OperationDbManager.get(context, operationId)
        val instruction = InstructionDbManager.get(context, instructionId)
        if (operation != null && instruction != null){
            when(result.code){
                ResultCode.SUCCESS -> {
                    operation.change2Complete(context, result)
                    instruction.change2Complete(context, result)
                }
                else -> {
                    operation.change2Fail(context, result)
                    instruction.change2Complete(context, result)
                }
            }
            instructionNotifyApi(context, instruction, result)
        }
    }

    /**
     * 指令通知接口
     * 数据格式：
     * {instructionId="abcd1234",original="{xxx}",result={code=1,msg="成功"},content="{xxx}",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun instructionNotifyApi(context: Context, instruction: InstructionModel, result: ResultBean = instruction.result) {
        var map = mutableMapOf<String, String?>()
        map["instructionId"] = instruction.instructionId
        map["original"] = URLDecoder.decode(instruction.original, "UTF-8")
        map["content"] = if (instruction.content != null) ConvertHelper.gson.toJson(instruction.content) else "{}"
        map["result"] = result.toString()

        var url: String
        if (instruction.notifyUrl.isNullOrEmpty()) {
            url = Config.API_INSTRUCTION_DEFAULT_NOTIFY
        }
        else {
            url =instruction.notifyUrl!!
        }

        VolleyRequestUtils.requestPost(context, url, "${Config.API_TAG_INSTRUCTION_NOTIFY}_${instruction.instructionId}", processPostParams(map),
                object : HttpListenerInterface(context) {

                    override fun onSuccess(obj: JSONObject) {
                        instruction.change2Feedback(context, result)
                    }
                }
        )
    }

    /**
     * 原始指令通知接口
     */
    fun instructionOriginalNotifyApi(context: Context, original: String, result: ResultBean) {
        var map = mutableMapOf<String, String?>()
        map["original"] = original
        map["result"] = result.toString()

        VolleyRequestUtils.requestPost(context, Config.API_INSTRUCTION_ORIGINAL_NOTIFY, Config.API_TAG_INSTRUCTION_NOTIFY, processPostParams(map),
                object : HttpListenerInterface(context) {
                }
        )
    }

    /**
     * 系统运行错误上报
     * 数据格式：
     * {log="{xxxx}",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun systemErrorNotify(context: Context, it: LogItem) {
        var map = mutableMapOf<String, String?>()
        map["log"] = ConvertHelper.gson.toJson(it)

        VolleyRequestUtils.requestPost(context, Config.API_SYSTEM_ERROR_NOTIFY, Config.API_TAG_SYSTEM_ERROR_NOTIFY, processPostParams(map),
                object : HttpListenerInterface(context) {
                }
        )
    }
    //endregion

    //region 注册

    /**
     * 注册时处理完成指令后将处理结果反馈服务器
     * 数据格式：
     * {message="{xxxx}",result={code=1,msg="成功"},deviceInfo="{xxxxx}",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun registrationNotifyApi(context: Context, message: String, result: RegistrationResultBean) {
        var map = mutableMapOf<String, String?>()

        map["message"] = message
        map["result"] = result.toString()
        map["deviceInfo"] = ConvertHelper.gson.toJson(DeviceSettingUtils.getSystemInfo(context))

        VolleyRequestUtils.requestPost(context, Config.API_REGISTRATION_URL_NOTIFY, Config.API_TAG_REGISTRATION_URL_NOTIFY, processPostParams(map),
                object : HttpListenerInterface(context) {}
        )
    }
    //endregion

    //region 数据同步

    /**
     * 同步播放器列表
     * 数据格式：
     * {
     *      code=1,
     *      dataList=[
     *          {playerId="player1", width=1024, height=100, x=0, y=0, sort=1 },
     *          {playerId="player2", width=1024, height=720, x=0, y=100, sort=2 }]
     * }
     */
    fun syncPlayerList(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}){
        VolleyRequestUtils.requestGet(context, "${Config.API_SYN_PLAYER_LIST}?${processGetParams(arg)}", Config.API_TAG_SYN_PLAYER_LIST,
                object : HttpListenerInterface(context) {
                    override fun onSuccess(obj: JSONObject) {
                        val dataList = obj.getJSONArray("dataList")
                        PlayerDbManager.deleteAll(context)
                        // 删除过时的播放器
                        /*val players = PlayerDbManager.getAll(context)
                        players.filter {
                            var isNotContain = true
                            for (i in 0 until dataList.length()) {
                                val item = dataList.get(i) as JSONObject
                                if (it.playerId == item.getString("playInfoId")){
                                    isNotContain = false
                                    break
                                }
                            }
                            isNotContain
                        }.forEach {
                            PlayerDbManager.delete(context,it.playerId)
                        }*/

                        //更新或新增播放器
                        for (i in 0 until dataList.length()) {
                            val item = dataList.get(i) as JSONObject
                            val player = PlayerModel(
                                    playerId = item.getString("playerId"),
                                    width = item.getInt("width"),
                                    height = item.getInt("height"),
                                    x = item.getDouble("x").toFloat(),
                                    y = item.getDouble("y").toFloat(),
                                    sort = item.getInt("sort")
                            )
                            player.save(context)
                        }
                        PlayerListManager.refreshPlayerList()
                        PlayerListManager.startAllLoop(context)
                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
    }

    /**
     * 同步节目列表
     * 数据格式：
     * {
     *      code=1,
     *      playInfo=[
     *          {playInfoId="id1", launcher="index.html", duration=50000, fileMd5="abcdefgmd5", type=1},
     *          {playInfoId="id2", launcher="index.html", duration=50000, fileMd5="hijklmnmd5", type=1}
     *          {playInfoId="id3", launcher="video.mp4"", duration=50000, fileMd5="abcdefgmd5", type=2},
     *          {playInfoId="id4", launcher="video.mp4", duration=50000, fileMd5="hijklmnmd5", type=2}],
     *      playerPlayInfo=[
     *          {playInfoId="id1", playerId="player1",sort=1},
     *          {playInfoId="id2", playerId="player1",sort=2}
     *          {playInfoId="id3", playerId="player2",sort=1},
     *          {playInfoId="id4", playerId="player2",sort=2}]
     * }
     */
    fun syncPlayInfoList(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}){
        VolleyRequestUtils.requestGet(context, "${Config.API_SYN_PLAY_INFO_LIST}?${processGetParams(arg)}", Config.API_TAG_SYN_PLAY_INFO_LIST,
                object : HttpListenerInterface(context) {
                    override fun onSuccess(obj: JSONObject) {
                        //PlayInfoDbManager.deleteAll(context)
                        var oldPlayInfos = PlayInfoDbManager.getAll(context)
                        val playInfos = obj.getJSONArray("playInfo")
                        for (i in 0 until playInfos.length()) {
                            val item = playInfos.get(i) as JSONObject
                            var playInfo = oldPlayInfos.find { it.playInfoId == item.getString("playInfoId") }
                            if (playInfo != null){
                                playInfo.launcher = item.getString("launcher")
                                playInfo.duration = item.getLong("duration")
                                playInfo.fileMd5 = item.getString("fileMd5")
                                playInfo.type = PlayInfoType.valueOf(item.getString("type"))
                                oldPlayInfos.remove(playInfo)
                            }
                            else{
                                playInfo = PlayInfoModel(
                                        playInfoId = item.getString("playInfoId"),
                                        launcher = item.getString("launcher"),
                                        duration = item.getLong("duration"),
                                        fileMd5 = item.getString("fileMd5"),
                                        type = PlayInfoType.valueOf(item.getString("type"))
                                )
                            }
                            playInfo.save(context)
                        }
                        oldPlayInfos.forEach { it.delete(context) }

                        PlayerPlayInfoDbManager.deleteAll(context)
                        val playerPlayInfos = obj.getJSONArray("playerPlayInfo")
                        for (i in 0 until playerPlayInfos.length()) {
                            val item = playerPlayInfos.get(i) as JSONObject
                            val playerPlayInfo = PlayerPlayInfoModel(
                                    playInfoId = item.getString("playInfoId"),
                                    playerId = item.getString("playerId"),
                                    sort = item.getInt("sort")
                            )
                            //PlayerPlayInfoDbManager.insert(context, playerPlayInfo)
                            playerPlayInfo.save(context)
                        }

                        SecurityUtils.checkAllPlayInfo(context)
                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
    }

    /**
     * 同步节目资源列表
     * 数据格式：
     * {
     *      code=1,
     *      dataList=[
     *          {playInfoId="id1", fileMd5="abcdefgmd5"},
     *          {playInfoId="id2", fileMd5="hijklmnmd5"}]
     * }
     */
    fun syncPlayInfoResourcesList(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}){
        VolleyRequestUtils.requestGet(context, "${Config.API_SYN_PLAY_INFO_RESOURCES_LIST}?${processGetParams(arg)}", Config.API_TAG_SYN_PLAY_INFO_RESOURCES_LIST,
                object : HttpListenerInterface(context) {
                    override fun onSuccess(obj: JSONObject) {
                        val dataList = obj.getJSONArray("dataList")
                        val files = File("${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}").listFiles()
                        files.filter {
                            var isNotContain = true
                            for (i in 0 until dataList.length()) {
                                val item = dataList.get(i) as JSONObject
                                if (it.name == item.getString("playInfoId")){
                                    isNotContain = false
                                    break
                                }
                            }
                            isNotContain
                        }.forEach {
                            it.deleteOnExit()
                        }
                        Thread(Runnable(){
                            run(){
                                for (i in 0 until dataList.length()) {
                                    val item = dataList.get(i) as JSONObject
                                    //如果资源文件校验不合法，则重新下载资源
                                    if (!SecurityUtils.isPathFilesValid("${Config.APP_DATA_PATH}/${item.getString("playInfoId")}", item.getString("fileMd5"))){
                                        DownloadUtils.downloadResources(context, item.getString("playInfoId"))
                                    }
                                }
                            }
                        }).start()

                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
    }

    /**
     * 同步操作字典
     * 获取的数据格式：
     * {
     *      code=1,
     *      version="1.0"
     *      dataList=[
     *          {key="key1", type=1, method="test1"},
     *          {key="key2", type=1, method="test2"}]
     * }
     */
    fun syncOperationDictionary(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}) {
        VolleyRequestUtils.requestGet(context, "${Config.API_SYN_OPERATION_DICTIONARY}?${processGetParams(arg)}", Config.API_TAG_SYN_OPERATION_DICTIONARY,
                object : HttpListenerInterface(context) {
                    override fun onSuccess(obj: JSONObject) {
                        val dataList = obj.getJSONArray("dataList")
                        Config.OPERATION_DICTIONARY_VERSION = obj.optString("version")
                        OperationDictionaryDbManager.deleteAll(context)
                        for (i in 0 until dataList.length()) {
                            val item = dataList.get(i) as JSONObject
                            val operationDictionary = OperationDictionaryBean(
                                    key = item.getString("key"),
                                    type = OperateType.values()[item.getInt("type")],
                                    method = item.getString("method")
                            )
                            OperationDictionaryDbManager.insert(context, operationDictionary)
                        }
                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
    }

    /**
     * 同步定时器
     * 获取的数据格式：
     * {
     *      code=1,
     *      dataList=[
     *          {alarmId="alarmId1", sign="abcd1234", time=123456789, dateSetting="{single:{date:\"2018-04-27,2018-04-28\"},repeat:{dayInWeek:\"1,3,5\",dayInMonth:\"1,10,15,30\",weekInMoth:\"1,3\",monthInYear:\"1,2,7,8\"}}",notifyUrl="/",key="test"},
     *          {alarmId="alarmId2", sign="efgh56789", time=123456789, dateSetting="{single:{date:\"2018-04-27,2018-04-28\"},repeat:{dayInWeek:\"1,3,5\",dayInMonth:\"1,10,15,30\",weekInMoth:\"1,3\",monthInYear:\"1,2,7,8\"}}",notifyUrl="/",key="test"}]
     * }
     */
    fun syncAlarm(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}) {
        VolleyRequestUtils.requestGet(context, "${Config.API_SYN_ALARM}?${processGetParams(arg)}", Config.API_TAG_SYN_ALARM,
                object : HttpListenerInterface(context) {
                    override fun onSuccess(obj: JSONObject) {
                        val dataList = obj.getJSONArray("dataList")
                        AlarmDbManager.deleteAll(context)
                        for (i in 0 until dataList.length()) {
                            val item = dataList.get(i) as JSONObject
                            val alarm = AlarmModel(
                                    alarmId = item.getString("alarmId"),
                                    sign = item.getString("sign"),
                                    time = item.getString("time"),
                                    dateSetting = item.getString("dateSetting"),
                                    notifyUrl = item.getString("notifyUrl"),
                                    key = item.getString("key")
                            )

                            if (item.has("params")) {
                                alarm.params = ConvertHelper.gson.fromJson(item.getString("params"), object : TypeToken<SortedMap<String, String?>>() {}.type)
                            }
                            if (item.has("content")) {
                                alarm.content = ConvertHelper.gson.fromJson(item.getString("content"), object : TypeToken<SortedMap<String, String?>>() {}.type)
                            }
                            AlarmDbManager.insert(context, alarm)
                        }
                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
    }
    //endregion

    //region 获取设备信息
    /**
     * 上传APP版本号和包名
     * 数据格式：
     * {versionName="1.0",packageName="com.lsinfo.maltose",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun postAppVersion(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        arg["versionName"] = DeviceSettingUtils.getVersionName(context)
        arg["packageName"] = DeviceSettingUtils.getPackageName(context)

        VolleyRequestUtils.requestPost(context, Config.API_POST_APP_VERSION, Config.API_TAG_POST_APP_VERSION, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * 上传系统信息
     * 数据格式：
     * {systemVersion="4.4",
     * systemModel="Maltose",
     * deviceBrand="lsinfo",
     * cpuName="rk3288",
     * totalRam="2048",
     * sdCardTotalMemory="8192",
     * macAddress="11:22:aa:bb:cc",
     * screenResolutionWidth="800",
     * screenResolutionHeight="600",
     * screenOrientation="0",
     * screenBrightnessMode="1",
     * screenBrightness="100",
     * volume="255",
     * token="abcde",
     * timestamp=987654321,
     * sign="fghi123456"}
     */
    fun postSystemInfo(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        arg.putAll(DeviceSettingUtils.getSystemInfo(context))

        VolleyRequestUtils.requestPost(context, Config.API_POST_SYSTEM_INFO, Config.API_TAG_POST_SYSTEM_INFO, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * 上传系统播放信息
     * 数据格式：
     * {playInfo={xxxxx},playerPlayInfo={xxxxx},player={xxxxx}, token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun postSystemPlayInfo(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean{
        var result = ResultBean(ResultCode.SUCCESS)
        arg["playInfo"] = PlayInfoDbManager.getAll(context).toString()
        arg["playerPlayInfo"] = PlayerPlayInfoDbManager.getAll(context).toString()
        arg["player"] = PlayerDbManager.getAll(context).toString()

        VolleyRequestUtils.requestPost(context, Config.API_POST_SYSTEM_PLAY_INFO, Config.API_TAG_POST_SYSTEM_PLAY_INFO, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * 上传SDCard信息
     * 数据格式：
     * {sdCardTotalMemory="8192",sdCardAvailableMemory="6144",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun postSDCardInfo(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        arg["sdCardTotalMemory"] = DeviceSettingUtils.getSDCardTotalMemory().toString()
        arg["sdCardAvailableMemory"] = DeviceSettingUtils.getSDCardAvailableMemory().toString()

        VolleyRequestUtils.requestPost(context, Config.API_POST_SDCARD_INFO, Config.API_TAG_POST_SDCARD_INFO, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * 上传内存信息
     * 数据格式：
     * {totalRam="2048",availableRam="1024",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun postRamInfo(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        arg["totalRam"] = DeviceSettingUtils.getTotalRam().toString()
        arg["availableRam"] = DeviceSettingUtils.getAvailableRam(context).toString()

        VolleyRequestUtils.requestPost(context, Config.API_POST_RAM_INFO, Config.API_TAG_POST_RAM_INFO, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * 上传网络状况
     * 数据格式：
     * {isNetworkConnected="true",isWifiConnected="false",isMobileConnected="true",connectedType="2",wifiInfo="{xxx}",wifiList="{[wifiList}]",token="abcde",timestamp=987654321,sign="fghi123456"}
     */
    fun postNetworkInfo(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        arg["isNetworkConnected"] = DeviceSettingUtils.isNetworkConnected(context).toString()
        arg["isWifiConnected"] = DeviceSettingUtils.isWifiConnected(context).toString()
        arg["isMobileConnected"] = DeviceSettingUtils.isMobileConnected(context).toString()
        arg["connectedType"] = DeviceSettingUtils.getConnectedType(context).toString()
        arg["wifiInfo"] = ConvertHelper.gson.toJson(DeviceSettingUtils.getWifiInfo(context))
        arg["wifiList"] = ConvertHelper.gson.toJson(DeviceSettingUtils.getWifiList(context))

        VolleyRequestUtils.requestPost(context, Config.API_POST_NETWORK_INFO, Config.API_TAG_POST_NETWORK_INFO, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    //endregion

    //region APP更新
    /**
     * 检查APP更新版本
     * 获取的数据格式：
     * {
     *      code=1,
     *      dataList=[
     *          {versionName="1.0",packageName="com.lsinfo.maltose",versionCode="1",id="abcdefg1234"}
     *      ]
     * }
     */
    fun checkUpgrade(context: Context, arg: MutableMap<String, String?> = mutableMapOf(), callBack:(callBackArg: ResultBean) -> Unit = {}):ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)

        VolleyRequestUtils.requestPost(context, Config.API_CHECK_UPGRADE, Config.API_TAG_CHECK_UPGRADE, processPostParams(arg),
                object : HttpListenerInterface(context) {

                    override fun onSuccess(obj: JSONObject) {
                        val dataList = obj.getJSONArray("dataList")
                        for (i in 0 until dataList.length()) {
                            val item = dataList.get(i) as JSONObject

                            val remoteVersionName = item.optString("versionName")
                            val remoteVersionCode = item.optInt("versionCode")
                            val remotePackageName = item.optString("packageName")

                            if (DeviceSettingUtils.getVersionName(context) != remoteVersionName ||
                                DeviceSettingUtils.getVersionCode(context) != remoteVersionCode ||
                                DeviceSettingUtils.getPackageName(context) != remotePackageName){
                                DownloadUtils.downloadApk(context, item.optString("id"))
                            }
                        }
                        result.code = ResultCode.SUCCESS
                    }

                    override fun onCallBack(result: ResultBean) {
                        if (arg.contains("instructionId") && arg.contains("operationId"))
                            operationCallBack(context, arg["instructionId"].toString(), arg["operationId"].toString(), result)
                        callBack(result)
                    }
                }
        )
        return result
    }

    /**
     * APP升级处理结果反馈
     */
    fun upgradeNotify(context: Context, appId: String, success: Boolean, callBack:(callBackArg: ResultBean) -> Unit = {}) {
        var map = mutableMapOf<String, String?>()
        map["appId"] = appId
        map["success"] = success.toString()

        VolleyRequestUtils.requestPost(context, Config.API_UPGRADE_NOTIFY, Config.API_TAG_UPGRADE_NOTIFY, processPostParams(map),
                object : HttpListenerInterface(context) {
                }
        )
    }
    //endregion
}