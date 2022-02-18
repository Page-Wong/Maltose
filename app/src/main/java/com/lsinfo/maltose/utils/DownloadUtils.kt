package com.lsinfo.maltose.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Message
import com.lsinfo.maltose.Config
import java.io.File

/**
 * Created by G on 2018-04-18.
 */
object DownloadUtils {
    var resourcesDownloadingMap: MutableMap<Long, String> = mutableMapOf()
    var apkDownloadingMap: MutableMap<Long, String> = mutableMapOf()

    fun downloadResources(context: Context, playInfoId: String?, isEnforcing: Boolean = false) {
        if (playInfoId ==null) return

        val downManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (isEnforcing) {//如果强制执行则先清除之前的下载
            resourcesDownloadingMap.filterValues { it == playInfoId }.entries.forEach {
                downManager.remove(it.key)
                resourcesDownloadingMap.remove(it.key)
            }
        }
        if (resourcesDownloadingMap.containsValue(playInfoId)) return

        val oldFile = File("${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/$playInfoId");
        if (oldFile != null && oldFile.exists()) {
            oldFile.delete();
        }

        var params = mutableMapOf<String,String?>()
        params["playInfoId"] = playInfoId

        val request = DownloadManager.Request(Uri.parse("${Config.RESOURCES_DOWNLOAD}?${HttpHandler.processGetParams(params)}"))
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle("下载")
        request.setDescription(playInfoId)
        request.setAllowedOverRoaming(true)
        //设置文件存放目录
        request.setDestinationInExternalPublicDir(Config.APP_TEMP_PATH, playInfoId)
        val id = downManager.enqueue(request)
        resourcesDownloadingMap[id] = playInfoId
    }

    /**
     * 资源压缩文件下载后进行解压，如解压成功则校验文件是否合法
     */
    fun downloadResourcesCallBack(context: Context, playInfoId: String?){
        if (playInfoId == null) return
        File("${Config.APP_DATA_PATH}/$playInfoId").delete()
        //try {
        val handle = object: Handler(){
            override fun handleMessage(msg: Message?) {
                if(msg?.obj == "checkPlayInfo"){
                    SecurityUtils.checkPlayInfo(context, playInfoId)
                }
            }
        }

        Thread(Runnable {
            ZipUtils.upZipFile(File("${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/$playInfoId"),"${Config.APP_DATA_PATH}/$playInfoId")
            File("${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/$playInfoId").delete()
            val msg = Message.obtain()
            msg.obj = "checkPlayInfo"
            //返回主线程执行
            handle.sendMessage(msg)
        }).start()
        /*}
        catch (e: Exception){
            LoggerHandler.fileLog.e(mapOf<String,String?>(
                    Pair("msg","解压出错"),
                    Pair("exception",e.message),
                    Pair("playInfoId",playInfoId)
            ))
        }*/
    }

    fun downloadApk(context: Context, itemId: String?, isEnforcing: Boolean = false) {
        if (itemId == null) return

        val downManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (isEnforcing) {//如果强制执行则先清除之前的下载
            apkDownloadingMap.filterValues { it == itemId }.entries.forEach {
                downManager.remove(it.key)
                apkDownloadingMap.remove(it.key)
            }
        }
        if (apkDownloadingMap.containsValue(itemId)) return

        val oldFile = File("${FileUtils.getSDPath()}/${Config.APP_TEMP_PATH}/$itemId.apk");
        if (oldFile != null && oldFile.exists()) {
            oldFile.delete();
        }

        var params = mutableMapOf<String,String?>()
        params["id"] = itemId

        val request = DownloadManager.Request(Uri.parse("${Config.APP_DOWNLOAD}?${HttpHandler.processGetParams(params)}"))
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle("下载")
        request.setDescription(itemId)
        request.setAllowedOverRoaming(true)
        //设置文件存放目录
        request.setDestinationInExternalPublicDir(Config.APP_TEMP_PATH,"$itemId.apk")
        val id = downManager.enqueue(request)
        apkDownloadingMap[id] = itemId
    }

    fun downloadApkCallBack(context: Context, itemId: String?) {
        if (itemId == null) return
        if (DeviceSettingUtils.installApk("$itemId.apk")){
            HttpHandler.upgradeNotify(context, itemId, true)
            //context.startActivity(context.packageManager.getLaunchIntentForPackage(DeviceSettingUtils.getPackageName(context)))
        }
        else{
            HttpHandler.upgradeNotify(context, itemId, false)
        }
    }

}