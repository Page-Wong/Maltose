package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.utils.FileUtils
import com.lsinfo.maltose.utils.SecurityUtils
import java.io.File


/**
 * Created by G on 2018-04-09.
 */
enum class PlayInfoStatus{
    NONE,
    VALID,
    INVALID
}
enum class PlayInfoType{
    None,
    Web,
    Video
}
class PlayInfoModel(
        var playInfoId: String,
        var launcher: String,//节目启动器，如 index.html video.mp4 等
        var duration: Long,//持续时间，如果持续时间为 0 则一直播放此节目
        var fileMd5: String,
        var type: PlayInfoType,
        var status: PlayInfoStatus = PlayInfoStatus.NONE,
        var sort: Int? = null//临时排序
        ): IPlayInfoModel {

    override fun save(context: Context): Boolean {
        return if (PlayInfoDbManager.get(context, playInfoId) == null){
            PlayInfoDbManager.insert(context, this) > 0
        }
        else{
            PlayInfoDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return PlayInfoDbManager.delete(context, playInfoId) > 0
    }

    val absolutePath = "${Config.APP_DATA_PATH}/$playInfoId"
    val absoluteFilePath = "${Config.APP_DATA_FILE_PATH}/$playInfoId"
    val launcherPath = "$absolutePath/$launcher"
    val launcherFilePath = "$absoluteFilePath/$launcher"

    override fun isValid(): Boolean {
        return status == PlayInfoStatus.VALID
    }
    override fun isLauncherExists(): Boolean {
        return File(launcherPath).exists()
    }
}