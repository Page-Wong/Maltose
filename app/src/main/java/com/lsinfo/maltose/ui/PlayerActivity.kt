package com.lsinfo.maltose.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.lsinfo.maltose.App
import com.lsinfo.maltose.BuildConfig
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.R
import com.lsinfo.maltose.db.*
import com.lsinfo.maltose.model.*
import com.lsinfo.maltose.ui.view.IPlayerView
import com.lsinfo.maltose.utils.*
import java.io.File


class PlayerActivity: Activity(), IPlayerView {
    private val tag = "PlayerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //去掉信息栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //设置全屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)
        LoggerHandler.runLog.d("PlayerActivity onCreate")

        val decorView = window.decorView
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

        /*playerPresenter = PlayerPresenter(this)
        playerPresenter.placePlayer()*/

    }

    override fun onStart() {
        super.onStart()
        //PlayerListManager.startAllLoop(this)
    }

    override fun onStop() {
        super.onStop()
        //PlayerListManager.stopAllLoop(this)
    }

    override fun onResume() {
        super.onResume()
        PlayerListManager.startAllLoop(App.getInstance())

    }

    override fun onPause() {
        super.onPause()
        //PlayerListManager.stopAllLoop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerListManager.stopAllLoop(App.getInstance())
    }

    //记录用户首次点击返回键的时间
    private var firstTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        //改写物理返回键的逻辑
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (BuildConfig.DEBUG) test()
//            return true
            val secondTime=System.currentTimeMillis()
            if(secondTime-firstTime>2000){
                firstTime=secondTime;
                return true;
            }else{
                PlayerListManager.stopAllLoop(App.getInstance())
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setClass(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun test(){
        if  (false) {
            LoggerHandler.runLog.d(tag, "getTotalRam:${DeviceSettingUtils.getTotalRam().toString()}")
            LoggerHandler.runLog.d(tag, "getAvailableRam:${DeviceSettingUtils.getAvailableRam(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getConnectedType:${DeviceSettingUtils.getConnectedType(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getCpuName:${DeviceSettingUtils.getCpuName().toString()}")
            LoggerHandler.runLog.d(tag, "getDeviceBrand:${DeviceSettingUtils.getDeviceBrand().toString()}")
            LoggerHandler.runLog.d(tag, "getMacAddress:${DeviceSettingUtils.getMacAddress(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getPackageName:${DeviceSettingUtils.getPackageName(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getSDCardAvailableMemory:${DeviceSettingUtils.getSDCardAvailableMemory().toString()}")
            LoggerHandler.runLog.d(tag, "getSDCardTotalMemory:${DeviceSettingUtils.getSDCardTotalMemory().toString()}")
            LoggerHandler.runLog.d(tag, "getScreenBrightness1:${DeviceSettingUtils.getScreenBrightness(App.getInstance()).toString()}")
            DeviceSettingUtils.setScreenBrightness(App.getInstance(),10)
            LoggerHandler.runLog.d(tag, "getScreenBrightness2:${DeviceSettingUtils.getScreenBrightness(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getScreenBrightnessMode1:${DeviceSettingUtils.getScreenBrightnessMode(App.getInstance()).toString()}")
            DeviceSettingUtils.setScreenBrightnessMode(App.getInstance(),1)
            LoggerHandler.runLog.d(tag, "getScreenBrightnessMode2:${DeviceSettingUtils.getScreenBrightnessMode(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getScreenOrientation1:${DeviceSettingUtils.getScreenOrientation().toString()}")
            DeviceSettingUtils.setScreenOrientation(DeviceSettingUtils.getScreenOrientation()!!+90)
            LoggerHandler.runLog.d(tag, "getScreenOrientation2:${DeviceSettingUtils.getScreenOrientation().toString()}")
            LoggerHandler.runLog.d(tag, "getScreenResolution:${DeviceSettingUtils.getScreenResolution(App.getInstance())[0]},${DeviceSettingUtils.getScreenResolution(App.getInstance())[1]}")
            LoggerHandler.runLog.d(tag, "getSystemModel:${DeviceSettingUtils.getSystemModel().toString()}")
            LoggerHandler.runLog.d(tag, "getSystemVersion:${DeviceSettingUtils.getSystemVersion().toString()}")
            LoggerHandler.runLog.d(tag, "getVersionName:${DeviceSettingUtils.getVersionName(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getVersionName:${DeviceSettingUtils.getVersionCode(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getVolume1:${DeviceSettingUtils.getVolume(App.getInstance()).toString()}")
            DeviceSettingUtils.setVolume(App.getInstance(), 10)
            LoggerHandler.runLog.d(tag, "getVolume2:${DeviceSettingUtils.getVolume(App.getInstance()).toString()}")
            LoggerHandler.runLog.d(tag, "getWifiInfo1:${ConvertHelper.gson.toJson(DeviceSettingUtils.getWifiInfo(App.getInstance()))}")
            DeviceSettingUtils.connectWifi(App.getInstance(),"LSinfo.com.cn","lswifi+9893-",3)
            LoggerHandler.runLog.d(tag, "getWifiInfo2:${ConvertHelper.gson.toJson(DeviceSettingUtils.getWifiInfo(App.getInstance()))}")
            LoggerHandler.runLog.d(tag, "getWifiList:${ConvertHelper.gson.toJson(DeviceSettingUtils.getWifiList(App.getInstance()))}")
            //DeviceSettingUtils.reboot()
            //DeviceSettingUtils.shutdown()
            val file = DeviceSettingUtils.screenshot()
            DeviceSettingUtils.closeWifi(App.getInstance())
            DeviceSettingUtils.openWifi(App.getInstance())
        }
        //DeviceSettingUtils.turnOffScreen()
        //DeviceSettingUtils.turnOnScreen()

    }
}
