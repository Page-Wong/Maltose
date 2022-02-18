package com.lsinfo.maltose.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.lsinfo.maltose.R
import com.lsinfo.wsmanager.WsManager
import com.lsinfo.wsmanager.listener.WsStatusListener
import com.lsinfo.maltose.App
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.WsMessageTypeEnum
import com.lsinfo.maltose.bean.registration.RegistrationDataBean
import com.lsinfo.maltose.bean.registration.RegistrationMessageBean
import com.lsinfo.maltose.bean.registration.RegistrationResultCode
import com.lsinfo.maltose.utils.*
import kotlinx.android.synthetic.main.activity_registration.*
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.ByteString
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class RegistrationActivity : Activity() {
    private val tag = "RegistrationActivity"

    private var wsManager : WsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去掉信息栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //设置全屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_registration)
        LoggerHandler.runLog.d("RegistrationActivity onCreate")
        startDefaultConnect()
    }

    override fun onResume() {
        super.onResume()
        //startDefaultConnect()
    }

    private val wsStatusListener = object : WsStatusListener() {
        override fun onOpen(response: Response) {
            LoggerHandler.commLog.i("RegistrationActivity onOpen:response=${response.toString()}")
        }

        override fun onMessage(text: String) {
            LoggerHandler.commLog.i("text=$text")

            val message = RegistrationMessageBean.fromString(text) ?: return

            LoggerHandler.runLog.d("message:${ConvertHelper.gson.toJson(message)}")
            var result = message.isVaild()
            LoggerHandler.runLog.d("isValid:${ConvertHelper.gson.toJson(result)}")

            if (result.code == RegistrationResultCode.SUCCESS) {
                if (message.dataBean!!.arguments!!["result"].toString() != "Success"){
                    Toast.makeText(this@RegistrationActivity, message.dataBean!!.arguments!!["message"]?.toString(), Toast.LENGTH_SHORT).show()
                }
                else {
                    when (message.messageType) {
                        WsMessageTypeEnum.Text -> {
                            LoggerHandler.runLog.d("Text:${message.data}")
                        }
                    //操作指令类型信息
                        WsMessageTypeEnum.ClientMethodInvocation -> {
                            LoggerHandler.runLog.d("ClientMethodInvocation:${message.data}")
                            when (message.dataBean.methodName) {
                                RegistrationDataBean.REGISTRATION_URL -> showQrCode(message.dataBean.arguments!!["url"].toString())

                                RegistrationDataBean.REGISTRATION_RESULT -> registrationCallback(message.dataBean.arguments!!)
                            }
                        }

                    //连接成功类型信息
                        WsMessageTypeEnum.ConnectionEvent -> {
                            Config.TOKEN = message.data
                        }
                    }
                }
            }
            else{
                imageView.setImageBitmap(null)
                Toast.makeText(this@RegistrationActivity, message.dataBean!!.arguments!!["message"]?.toString(), Toast.LENGTH_SHORT).show()
            }

            //处理完成后将处理结果反馈服务器
            HttpHandler.registrationNotifyApi(App.getInstance(), text, result )
        }

        override fun onMessage(bytes: ByteString) {
            LoggerHandler.commLog.i("RegistrationActivity WsManager-----onMessage:bytes=$bytes")
        }

        override fun onReconnect() {
            LoggerHandler.commLog.i("RegistrationActivity WsManager-----onReconnect")
        }

        override fun onClosing(code: Int, reason: String) {
            LoggerHandler.commLog.i("RegistrationActivity WsManager-----onClosing:code=${code.toString()},reason=$reason")
        }

        override fun onClosed(code: Int, reason: String) {
            LoggerHandler.commLog.i("RegistrationActivity onClosed:code=${code.toString()},reason=$reason")
        }

        override fun onFailure(t: Throwable, response: Response?) {
            LoggerHandler.commLog.w("RegistrationActivity onFailure:response=${response.toString()}", t)
        }
    }

    private fun startDefaultConnect() {
        if (wsManager != null) {
            wsManager?.stopConnect()
            wsManager = null
        }
        wsManager = WsManager.Builder(App.getInstance())
                .client(
                        OkHttpClient().newBuilder()
                                .pingInterval(15, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(true)
                                .build())
                .needReconnect(true)
                .wsUrl("${Config.WS_REGISTER_HOST}?deviceId=${SecurityUtils.des3EncodeECB(Config.REGISTRATION_KEY, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))}")
                .build()
        wsManager?.setWsStatusListener(wsStatusListener)
        wsManager?.startConnect()
    }

    /**
     * 将字符串转为二维码显示
     */
    private fun showQrCode(string: String){
        LoggerHandler.runLog.d("RegistrationActivity showQrCode:$string")
        val dm = resources.displayMetrics

        val size = if (dm.widthPixels > dm.heightPixels)
                        (dm.heightPixels*0.6).toInt()
                    else
                        (dm.widthPixels*0.6).toInt()
        val bitmap = QrCodeUtils.createQrCodeBitmap(string, size, size)
        imageView.setImageBitmap(bitmap)
    }

    /**
     * 注册完成时的处理
     */
    private fun registrationCallback(args: HashMap<String, Any?>){
        if (args["result"].toString() == "Success"){
            registrationSuccess(args["deviceId"].toString())
        }
        else{
            registrationFail(args["message"]?.toString())
        }
    }

    /**
     * 注册完成并成功的处理
     */
    private fun registrationSuccess(deviceId: String){
        val ed = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit()
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, deviceId)
        ed.commit()
        //Config.DEVICE_ID = deviceId;
        Toast.makeText(App.getInstance(), "注册成功！", Toast.LENGTH_SHORT)
        val intent = Intent()
        intent.setClass(App.getInstance(), MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * 注册完成但失败的处理
     */
    private fun registrationFail(msg: String?){
        Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT)
    }
}
