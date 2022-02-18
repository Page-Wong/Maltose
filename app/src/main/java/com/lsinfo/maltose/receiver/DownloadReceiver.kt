package com.lsinfo.maltose.receiver

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import com.lsinfo.maltose.utils.DownloadUtils
import com.lsinfo.maltose.utils.DownloadUtils.apkDownloadingMap
import com.lsinfo.maltose.utils.DownloadUtils.resourcesDownloadingMap
import com.lsinfo.maltose.utils.LoggerHandler
import java.io.File


/**
 * Created by G on 2018-04-18.
 */
class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val query = DownloadManager.Query()
            //在广播中取出下载任务的id
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            LoggerHandler.commLog.d("DownloadReceiver 下载完成 id=$id")
            query.setFilterById(id)
            val c = manager.query(query)
            if (c.moveToFirst()) {
                val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when(status){
                    DownloadManager.STATUS_SUCCESSFUL ->{
                        //获取文件下载路径
                        val uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)) ?: return
                        val file = File(Uri.parse(uri).path)
                        if (file == null || !file.exists()) return
                        when {
                            resourcesDownloadingMap.containsKey(id) -> {
                                val itemId = resourcesDownloadingMap[id]
                                resourcesDownloadingMap.remove(id)
                                LoggerHandler.commLog.d("DownloadReceiver 下载成功，为系统资源下载任务:id=$id,uri=$uri")
                                DownloadUtils.downloadResourcesCallBack(context, itemId)
                            }
                            apkDownloadingMap.containsKey(id) -> {
                                val itemId = apkDownloadingMap[id]
                                apkDownloadingMap.remove(id)
                                LoggerHandler.commLog.d("DownloadReceiver 下载成功，为系统APK下载任务:id=$id,uri=$uri")
                                DownloadUtils.downloadApkCallBack(context, itemId)
                            }
                            else -> {
                                LoggerHandler.commLog.d("DownloadReceiver 下载成功，与系统下载任务不匹配:id=$id,uri=$uri")
                            }
                        }
                    }
                    DownloadManager.STATUS_FAILED -> {//如果下载失败，则重新下载
                        LoggerHandler.commLog.e("DownloadReceiver 下载失败:id=$id,uri=${c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))}")
                        when {
                            resourcesDownloadingMap.containsKey(id) -> {
                                val itemId = resourcesDownloadingMap[id]
                                resourcesDownloadingMap.remove(id)
                                DownloadUtils.downloadResources(context, itemId)
                            }
                            apkDownloadingMap.containsKey(id) -> {
                                val itemId = resourcesDownloadingMap[id]
                                apkDownloadingMap.remove(id)
                                DownloadUtils.downloadApk(context, itemId)
                            }
                            else -> {
                                LoggerHandler.commLog.e("DownloadReceiver 下载失败，与系统下载任务不匹配:id=$id,uri=${c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))}")
                            }
                        }
                    }
                }


            }
        }
    }

}