package com.lsinfo.maltose.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import com.lsinfo.maltose.App
import com.lsinfo.maltose.R
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.service.InstructionService
import com.lsinfo.maltose.db.*
import com.lsinfo.maltose.model.*
import com.lsinfo.maltose.utils.*
import java.io.File


class MainActivity : Activity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //去掉信息栏
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //设置全屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)
        LoggerHandler.runLog.d("MainActivity onCreate")

        //测试环境跳过注册
        //if (BuildConfig.DEBUG) {
        /*val ed = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit()
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, "")
        ed.commit()*/
        //Config.DEVICE_ID = String();
        //}
        //如果deviceId或token为空，则表示未注册，跳转到注册页面

    }

    override fun onResume() {
        super.onResume()
        if (Config.DEVICE_ID.isEmpty() && !Config.SETTING_IS_LOCAL_PLAY){
            val intent = Intent()
            intent.setClass(App.getInstance(), RegistrationActivity::class.java)
            startActivity(intent)
        }
        else{
            initSetting()

            val serviceIntent = Intent()
            serviceIntent.setClass(App.getInstance(), InstructionService::class.java)
            startService(serviceIntent)
        }
    }

    private fun initSetting(){
        DeviceSettingUtils.setScreenBrightnessMode(App.getInstance(), Config.SETTING_SCREEN_BRIGHTNESS_MODE)
        DeviceSettingUtils.setScreenBrightness(App.getInstance(), Config.SETTING_SCREEN_BRIGHTNESS)
        DeviceSettingUtils.setVolume(App.getInstance(), Config.SETTING_VOLUME)
    }

    private fun initTestData(){
        if (PlayInfoDbManager.getAll(App.getInstance()).isNotEmpty()) return
        Thread(Runnable(){

            run(){
                PlayInfoDbManager.deleteAll(App.getInstance())
                PlayerDbManager.deleteAll(App.getInstance())
                PlayerPlayInfoDbManager.deleteAll(App.getInstance())
                OperationDictionaryDbManager.deleteAll(App.getInstance())
                var player1 = PlayerModel(
                        playerId = "player1",
                        width = DeviceSettingUtils.getScreenResolution(App.getInstance())[0],
                        height = DeviceSettingUtils.getScreenResolution(App.getInstance())[1],
                        x = 0F,
                        y = 0F,
                        sort = 1
                )
                /*var player1 = PlayerModel(
                        playerId = "player1",
                        width = DeviceSettingUtils.getScreenResolution(App.getInstance())[0],
                        height = 300,
                        x = 0F,
                        y = 0F,
                        sort = 1
                )

                var player2 = PlayerModel(
                        playerId = "player2",
                        width = DeviceSettingUtils.getScreenResolution(App.getInstance())[0],
                        height = (DeviceSettingUtils.getScreenResolution(App.getInstance())[1]-300)/3,
                        x = 0F,
                        y = (player1.height).toFloat(),
                        sort = 2
                )
                var player3 = PlayerModel(
                        playerId = "player3",
                        width = DeviceSettingUtils.getScreenResolution(App.getInstance())[0],
                        height = (DeviceSettingUtils.getScreenResolution(App.getInstance())[1]-300)/3,
                        x = 0F,
                        y = (player1.height+player2.height).toFloat(),
                        sort = 3
                )
                var player4 = PlayerModel(
                        playerId = "player4",
                        width = DeviceSettingUtils.getScreenResolution(App.getInstance())[0],
                        height = (DeviceSettingUtils.getScreenResolution(App.getInstance())[1]-300)/3,
                        x = 0F,
                        y = (player1.height+player2.height+player3.height).toFloat(),
                        sort = 4
                )*/
                PlayerDbManager.insert(App.getInstance(), player1)
                /*PlayerDbManager.insert(App.getInstance(), player2)
                PlayerDbManager.insert(App.getInstance(), player3)
                PlayerDbManager.insert(App.getInstance(), player4)*/
                var i=1

                while (i<=6){
                    val launcher = "video.mp4"
                    var duration = 0L
                    if (i%2 == 0) {
                        duration = 12000L
                    }
                    else {
                        duration = 220000L
                    }
                    val item = PlayInfoModel(
                            playInfoId = i.toString(),
                            launcher=launcher,
                            duration=duration,
                            fileMd5= SecurityUtils.md5Encrypt(FileUtils.getAllFiles(File("${Config.APP_DATA_PATH}/$i"), arrayListOf<File>())),
                            status = PlayInfoStatus.VALID,
                            type = PlayInfoType.Video
                    )
                    PlayInfoDbManager.insert(App.getInstance(),item)
                    PlayerPlayInfoDbManager.insert(App.getInstance(), PlayerPlayInfoModel(playerId = "player1", playInfoId = i.toString(), sort = i))
                    i++
                }

                /*while (i<=2){
                    val launcher = "index.html"
                    val item = PlayInfoModel(
                            playInfoId = i.toString(),
                            launcher=launcher,
                            duration=10000L,
                            fileMd5= SecurityUtils.md5Encrypt(FileUtils.getAllFiles(File("${Config.APP_DATA_PATH}/$i"), arrayListOf<File>())),
                            status = PlayInfoStatus.VALID,
                            type = PlayInfoType.Web
                    )
                    PlayInfoDbManager.insert(App.getInstance(),item)
                    PlayerPlayInfoDbManager.insert(App.getInstance(), PlayerPlayInfoModel(playerId = "player1", playInfoId = i.toString(), sort = i))
                    i++
                }
                while (i<=4){
                    val launcher = "video.mp4"
                    val item = PlayInfoModel(
                            playInfoId = i.toString(),
                            launcher=launcher,
                            duration=15000L,
                            fileMd5= SecurityUtils.md5Encrypt(FileUtils.getAllFiles(File("${Config.APP_DATA_PATH}/$i"), arrayListOf<File>())),
                            status = PlayInfoStatus.VALID,
                            type = PlayInfoType.Video
                    )
                    PlayInfoDbManager.insert(App.getInstance(),item)
                    PlayerPlayInfoDbManager.insert(App.getInstance(), PlayerPlayInfoModel(playerId = "player2", playInfoId = i.toString(), sort = i))
                    PlayerPlayInfoDbManager.insert(App.getInstance(), PlayerPlayInfoModel(playerId = "player3", playInfoId = i.toString(), sort = i))
                    PlayerPlayInfoDbManager.insert(App.getInstance(), PlayerPlayInfoModel(playerId = "player4", playInfoId = i.toString(), sort = i))
                    i++
                }*/
                //endregion
            }

        }).start()

    }
}
