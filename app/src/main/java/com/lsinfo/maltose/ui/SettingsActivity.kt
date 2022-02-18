package com.lsinfo.maltose.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.example.yf_rk3288_api.YF_RK3288_API_Manager
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.R
import com.lsinfo.maltose.db.*
import com.lsinfo.maltose.model.*
import com.lsinfo.maltose.service.InstructionService
import com.lsinfo.maltose.utils.FileUtils
import com.lsinfo.maltose.utils.UsbDiskDatabaseManager
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File
import java.util.*
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import com.lsinfo.maltose.utils.DeviceSettingUtils


class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        isLocalMode.isChecked = Config.SETTING_IS_LOCAL_PLAY
        if (Config.SETTING_IS_LOCAL_PLAY)
            updateLayout.visibility = View.VISIBLE
        else
            updateLayout.visibility = View.INVISIBLE

        isLocalMode.setOnCheckedChangeListener { compoundButton, b ->
            Config.SETTING_IS_LOCAL_PLAY = b
            if (Config.SETTING_IS_LOCAL_PLAY)
                updateLayout.visibility = View.VISIBLE
            else
                updateLayout.visibility = View.INVISIBLE
        }

        updateLocalData.setOnClickListener {
            val yfapi = YF_RK3288_API_Manager(App.getInstance())
            val UDISKpath = yfapi.yfgetUSBPath()
            if (File("$UDISKpath/maltose.apk").exists()){
                val pm = App.getInstance().packageManager
                val info = pm.getPackageArchiveInfo("$UDISKpath/maltose.apk", PackageManager.GET_ACTIVITIES)
                if (info.versionName != DeviceSettingUtils.getVersionName(App.getInstance()))
                    yfapi.adbcommand("pm install -r $UDISKpath/maltose.apk")
            }

            if (File("$UDISKpath/local.db").exists()){

                UsbDiskDatabaseManager.initManager(App.getInstance())
                // 获取管理对象，因为数据库需要通过管理对象才能够获取
                val mg = UsbDiskDatabaseManager.manager
                // 通过管理对象获取数据库
                val localDb = mg!!.getDatabase("local.db")
                // 对数据库进行操作
                InstructionDbManager.deleteAll(App.getInstance())
                OperationDbManager.deleteAll(App.getInstance())
                OperationDictionaryDbManager.deleteAll(App.getInstance())
                PlayInfoDbManager.deleteAll(App.getInstance())
                PlayerDbManager.deleteAll(App.getInstance())
                AlarmDbManager.deleteAll(App.getInstance())


                val playerCursor = localDb!!.query("player", null, "",
                        arrayOf(), null, null, "sort")
                if (playerCursor != null) {
                    while (playerCursor.moveToNext()){
                        var player = PlayerModel(
                                playerId = playerCursor.getString(playerCursor.getColumnIndex(PlayerDbManager.ID)),
                                width = playerCursor.getInt(playerCursor.getColumnIndex(PlayerDbManager.WIDTH)),
                                height = playerCursor.getInt(playerCursor.getColumnIndex(PlayerDbManager.HEIGHT)),
                                x = playerCursor.getFloat(playerCursor.getColumnIndex(PlayerDbManager.X)),
                                y = playerCursor.getFloat(playerCursor.getColumnIndex(PlayerDbManager.Y)),
                                sort = playerCursor.getInt(playerCursor.getColumnIndex(PlayerDbManager.SORT))
                        )

                        PlayerDbManager.insert(App.getInstance(), player)
                    }
                    playerCursor.close()
                }

                val playInfoCursor = localDb!!.query("play_info", null, "",
                        arrayOf(), null, null, null)
                if (playInfoCursor != null) {
                    while (playInfoCursor.moveToNext()){
                        var playInfo = PlayInfoModel(
                                playInfoId = playInfoCursor.getString(playInfoCursor.getColumnIndex(PlayInfoDbManager.ID)),
                                launcher = playInfoCursor.getString(playInfoCursor.getColumnIndex(PlayInfoDbManager.LAUNCHER)),
                                duration = playInfoCursor.getLong(playInfoCursor.getColumnIndex(PlayInfoDbManager.DURATION)),
                                fileMd5 = playInfoCursor.getString(playInfoCursor.getColumnIndex(PlayInfoDbManager.FILE_MD5)),
                                status = PlayInfoStatus.values()[playInfoCursor.getInt(playInfoCursor.getColumnIndex(PlayInfoDbManager.STATUS))],
                                type = PlayInfoType.values()[playInfoCursor.getInt(playInfoCursor.getColumnIndex(PlayInfoDbManager.TYPE))]
                        )

                        PlayInfoDbManager.insert(App.getInstance(), playInfo)
                    }
                    playInfoCursor.close()
                }

                val playerPlayInfoCursor = localDb!!.query("player_play_info", null, "",
                        arrayOf(), null, null, "sort")
                if (playerPlayInfoCursor != null) {
                    while (playerPlayInfoCursor.moveToNext()){
                        var playerPlayInfo = PlayerPlayInfoModel(
                                playerId = playerPlayInfoCursor.getString(playerPlayInfoCursor.getColumnIndex(PlayerPlayInfoDbManager.PLAYER_ID)),
                                playInfoId = playerPlayInfoCursor.getString(playerPlayInfoCursor.getColumnIndex(PlayerPlayInfoDbManager.PLAY_INFO_ID)),
                                sort = playerPlayInfoCursor.getInt(playerPlayInfoCursor.getColumnIndex(PlayerPlayInfoDbManager.SORT))
                        )

                        PlayerPlayInfoDbManager.insert(App.getInstance(), playerPlayInfo)
                    }
                    playerPlayInfoCursor.close()
                }
            }
            if (File("$UDISKpath/com.lsinfo.maltose").exists()){
                updateText.text = "正在更资源"
                Handler().post({
                    kotlin.run {
                        FileUtils.copyFolder("$UDISKpath/com.lsinfo.maltose", Config.APP_PATH)
                        updateText.text = "更新完成"
                    }
                })
            }
        }
    }
}
