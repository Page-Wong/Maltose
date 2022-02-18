package com.lsinfo.maltose.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.VideoView
import com.lsinfo.maltose.App
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.db.PlayerDbManager
import com.lsinfo.maltose.db.PlayerPlayInfoDbManager
import com.lsinfo.maltose.runnable.LoopPlayRunnable
import com.lsinfo.maltose.ui.PlayerActivity
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.JsBridgeUtils
import com.lsinfo.maltose.utils.LoggerHandler
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*


/**
 * Created by G on 2018-04-09.
 */
enum class PlayerStatus{
    NONE,
    PREPARING,
    PLAYING,
    FINISH
}
enum class PlayerMode{
    SINGLE,
    LOOP
}
class PlayerModel(
        val playerId: String,
        var width: Int,
        var height: Int,
        var x: Float,
        var y: Float,
        var sort: Int,
        var view: View? = null,
        var startTime: Date? = null,
        var status: PlayerStatus = PlayerStatus.NONE,
        var mode: PlayerMode = PlayerMode.LOOP,
        private var loopController: LoopPlayRunnable? = null,
        var playInfo: PlayInfoModel? = null): IPlayerModel {
    override fun save(context: Context): Boolean {
        return if (PlayerDbManager.get(context, playerId) == null){
            PlayerDbManager.insert(context, this) > 0
        }
        else{
            PlayerDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return PlayerDbManager.delete(context, playerId) > 0
    }

    override fun getPlayInfoList(context: Context): MutableList<PlayInfoModel> {
        val pps = PlayerPlayInfoDbManager.getAll(context).filter { it.playerId == playerId }.sortedBy { it.sort }.toMutableList()
        var playList = mutableListOf<PlayInfoModel>()
        pps.forEach {
            var playInfo = PlayInfoDbManager.get(context, it.playInfoId)
            if (playInfo != null){
                playInfo.sort = pps.indexOf(it)
                playList.add(playInfo)
            }
        }
        playList.sortBy { it.sort }
        return playList
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun play(context: Context, playInfoMode: PlayInfoModel?): ResultBean {
        //首先结束正在播放的节目，然后再播放下一个节目
        var result = changeStatus(context, PlayerStatus.FINISH)

        this.playInfo = playInfoMode
        if (playInfo == null){
            result.code = ResultCode.PLAY_INFO_NONE
        }
        else {
            result = changeStatus(context, PlayerStatus.PREPARING)
        }
        if (result.isSuccess()) {
            // 打开节目地址
            val url = playInfo!!.launcherFilePath//"${playInfo!!.absoluteFilePath}/${playInfo!!.launcher}"
                when (playInfoMode!!.type) {
                    PlayInfoType.Web -> {
                        result = addWebView()
                        if (result.isSuccess()){
                            (view as WebView).loadUrl(url)
                        }
                    }
                    PlayInfoType.Video -> {
                        result = addVideoView()
                        if (result.isSuccess()){
                            (view as VideoView).setVideoPath(url)
                            (view as VideoView).start()
                        }
                    }
                else -> {
                    result.code = ResultCode.PLAYER_TYPE_INVALID
                }
            }
            if (result.isSuccess()) result = changeStatus(context, PlayerStatus.PLAYING)
            if (result.isSuccess()){
                //刷新循环播放定时器
                loopController?.refreshDuration()
            }
        }

        return result
    }

    override fun playNext(context: Context): ResultBean {
        var result = ResultBean()
        return try {
            if(!isPlayInfoReady(context)) {
                result.code = ResultCode.PLAY_INFO_UNREADY
                result = stop(context, result)
            }
            else{
                val nextPlayInfo = getNextPlayInfo(context)
                if (nextPlayInfo == null){
                    result.code = ResultCode.PLAY_INFO_NONE

                }
                else if (!nextPlayInfo.isValid()){
                    result.code = ResultCode.PLAY_INFO_INVALID
                }
                else if (!nextPlayInfo.isLauncherExists()){
                    result.code = ResultCode.PLAY_INFO_LAUNCHER_NONE
                }
                else {
                    result = play(context, nextPlayInfo)
                }
                if (!result.isSuccess()) {
                    result = skip(context, result)
                }
            }
            result
        }catch (e: Exception){
            LoggerHandler.crashLog.e(e)
            result.code = ResultCode.PLAYER_PLAY_NEXT_ERROR
            result.exception = e
            result = skip(context, result)
            result

        }
    }

    /**
     * 停止播放
     */
    override fun stop(context: Context, r: ResultBean): ResultBean {
        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "stop"),
                Pair("result", r.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return try {
            var result = changeStatus(context, PlayerStatus.FINISH)
            stopLoop(context)
            if (playInfo != null && playInfo!!.type == PlayInfoType.Video) {
                addVideoView()
                (view as VideoView).stopPlayback()
            }
            else {
                addWebView()
                (view as WebView).loadData("<body bgcolor='#000000'><body>", "text/html", "utf-8")
            }
            playInfo = null
            result
        }
        catch (e:Exception){
            ResultBean(code=ResultCode.PLAYER_STOP_ERROR, exception = e)
        }
    }

    /**
     * 开启连续播放
     */
    override fun startLoop(context: Context): ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        if (!isReady()) {
            result.code = ResultCode.PLAYER_UNREADY
            return result
        }
        if (loopController == null) {
            loopController = LoopPlayRunnable(context, this)
        }
        if (!loopController!!.isRunning)
            loopController?.run()
        return result
    }

    /**
     * 停止连续播放
     */
    override fun stopLoop(context: Context): ResultBean {
        var result = ResultBean(ResultCode.SUCCESS)
        if (!isReady()) {
            result.code = ResultCode.PLAYER_UNREADY
            return result
        }
        if (loopController == null){
            result.code = ResultCode.PLAYER_LOOP_CONTROLLER_NONE
            return result
        }
        loopController!!.stop()
        return result
    }

    /**
     * 跳过本内容
     */
    override fun skip(context: Context, result: ResultBean): ResultBean {
        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "skip"),
                Pair("result", result.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return playNext(context)
    }

    /**
     * 获取下一个对象，如果没有则返回第一个对象，如果是单节目循环，则返回当前节目
     */
    override fun getNextPlayInfo(context: Context): PlayInfoModel? {
        val playList = getPlayInfoList(context)
        if (mode == PlayerMode.SINGLE){
            if (playInfo == null){
                return if (playList.isEmpty()) null else playList[0]
            }
            return playInfo
        }
        var items = playList.filter { it.sort!! > playInfo?.sort?:-1 && it.status == PlayInfoStatus.VALID }.sortedBy { it.sort }.toMutableList()

        return if (items.isEmpty()){
            val list = playList.filter { it.status == PlayInfoStatus.VALID }.sortedBy { it.sort }.toMutableList()
            if (list.isEmpty())
                null
            else
                list[0]
        }
        else
            items[0]
    }

    /**
     * 是否存在可播放的节目
     */
    override fun isPlayInfoReady(context: Context): Boolean{
        val items = getPlayInfoList(context)
        return items.any { it.isValid() && it.isLauncherExists() }
    }

    override fun changeStatus(context: Context, status: PlayerStatus):ResultBean {
        this.status = status
        return when(status) {
            PlayerStatus.PREPARING -> preparing(context)
            PlayerStatus.PLAYING -> playing(context)
            PlayerStatus.FINISH -> finish(context)
            PlayerStatus.NONE -> none(context)
        }
    }

    private fun preparing(context: Context): ResultBean{
        var result = ResultBean(ResultCode.SUCCESS)
        if (result.isSuccess() && playInfo == null){
            result.code = ResultCode.PLAY_INFO_NONE
        }
        if (result.isSuccess() && !playInfo!!.isValid()) {
            result.code = ResultCode.PLAY_INFO_INVALID
        }

        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "start"),
                Pair("result", result.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return result
    }

    private fun playing(context: Context): ResultBean{
        var result = ResultBean(ResultCode.SUCCESS)
        startTime = Date()

        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "playing"),
                Pair("result", result.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return result
    }

    private fun finish(context: Context): ResultBean{
        var result = ResultBean(ResultCode.SUCCESS)
        if (playInfo == null) return result

        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "finish"),
                Pair("result", result.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return result
    }

    private fun none(context: Context): ResultBean{
        var result = ResultBean(ResultCode.PLAYER_NONE)

        LoggerHandler.playLog.i(mapOf<String, String>(
                Pair("state", "none"),
                Pair("result", result.toString()),
                Pair("playerId", playerId),
                Pair("playInfo", ConvertHelper.gson.toJson(playInfo))
        ))
        return result
    }

    /**
     * 增加 WebView 类型的播放器
     */
    private fun addWebView():ResultBean {
        if (App.mCurrentActivity == null || App.mCurrentActivity !is PlayerActivity){
            return ResultBean(code = ResultCode.ACTIVITY_ERROR)
        }
        var context = App.mCurrentActivity!!
        var oldView = context.layout.findViewWithTag<View>(this.playerId)
        if (oldView!=null && oldView is WebView && oldView.layoutParams.width == this.width && oldView.layoutParams.height == this.height && oldView.translationX == this.x && oldView.translationY == this.y){
            this.view = oldView
            return ResultBean(ResultCode.SUCCESS);
        }

        var webView = WebView(context)
        webView.layoutParams = ConstraintLayout.LayoutParams(this.width, this.height)
        webView.translationX = this.x
        webView.translationY = this.y

        WebView.setWebContentsDebuggingEnabled(true)
        val webSettings = webView.settings

        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.javaScriptEnabled = true
        webSettings.blockNetworkLoads = true
        webSettings.blockNetworkImage = true
        webSettings.allowUniversalAccessFromFileURLs = false
        webSettings.allowFileAccessFromFileURLs = false
        //webSettings.mixedContentMode = MIXED_CONTENT_NEVER_ALLOW

        webView.webViewClient = PlayerWebViewClient(context, this)
        webView.webChromeClient = PlayerWebChromeClient(context, this)
        webView.addJavascriptInterface(JsBridgeUtils(), "app")
        webView.tag = this.playerId

        context.layout.removeView(oldView)
        context.layout.addView(webView)
        this.view = webView
        return ResultBean(ResultCode.SUCCESS)
    }

    /**
     * 增加 WebView 类型的播放器
     */
    private fun addVideoView(): ResultBean {
        if (App.mCurrentActivity == null || App.mCurrentActivity !is PlayerActivity){
            return ResultBean(code = ResultCode.ACTIVITY_ERROR)
        }
        var context = App.mCurrentActivity!!
        var oldView = context.layout.findViewWithTag<View>(this.playerId)
        if (oldView!=null && oldView is LinearLayout && oldView.layoutParams.width == this.width && oldView.layoutParams.height == this.height && oldView.translationX == this.x && oldView.translationY == this.y){
            var oldVideoView = oldView.findViewWithTag<VideoView>("${this.playerId}_VideoView")
            if (oldVideoView !=null){
                this.view = oldVideoView
                return ResultBean(ResultCode.SUCCESS)
            }
        }

        var ll = LinearLayout(context)
        ll.layoutParams = ConstraintLayout.LayoutParams(this.width, this.height)
        ll.translationX = this.x
        ll.translationY = this.y
        ll.gravity = Gravity.CENTER

        var view = VideoView(context)
        view.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

        //处理视频播放失败
        view.setOnErrorListener { mp, what, extra ->
            LoggerHandler.playLog.e(mapOf<String, String>(Pair("msg","VideoView OnErrorListener"),Pair("what",what.toString()),Pair("extra",extra.toString())))
            playNext(context)
            true
        }
        view.tag = "${this.playerId}_VideoView"
        ll.addView(view)
        ll.tag = this.playerId

        context.layout.removeView(oldView)
        context.layout.addView(ll)
        this.view = view
        return ResultBean(ResultCode.SUCCESS)
    }

    public fun destroy(): ResultBean{
        if (App.mCurrentActivity == null || App.mCurrentActivity !is PlayerActivity){
            return ResultBean(code = ResultCode.ACTIVITY_ERROR)
        }
        var context = App.mCurrentActivity!!
        stop(context, ResultBean(ResultCode.PLAYER_REMOVE_VIEW))
        if (this.view != null)
            context.layout.removeView(this.view)
        return ResultBean(ResultCode.SUCCESS)
    }

    private inner class PlayerWebViewClient(val context: Context, val player: PlayerModel) : WebViewClient() {

        fun isValid(url: String?):Boolean{
            Log.d("PlayerWebViewClient","isValid playerId=${player.playerId},path=${player.playInfo?.absoluteFilePath},url=$url")
            return url != null && player.playInfo != null && url.startsWith(player.playInfo!!.absoluteFilePath)
        }

        /**
         * 打开页面前先检测页面地址是否合法
         */
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (isValid(url) || url=="data:text/html,"){
                Log.d("PlayerWebViewClient", "onPageStarted success:$url")


                super.onPageStarted(view, url, favicon)
            }
            else{
                LoggerHandler.playLog.e(mapOf<String, String>(Pair("msg","PlayerWebViewClient onPageStarted"),Pair("url",url?:"")))
                view?.stopLoading()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Log.d("PlayerWebViewClient", "onPageFinished: $url")
        }

        /**
         * 检测打开的URL是否合法，如不合法则播放下一个节目
         */
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return if (isValid(url)){
                Log.d("PlayerWebViewClient", "shouldOverrideUrlLoading success:$url")
                false
            } else{
                Log.d("PlayerWebViewClient", "shouldOverrideUrlLoading fail:$url")
                true
            }
        }

        /**
         * 同上
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return shouldOverrideUrlLoading(view,request?.url.toString())
        }

        /**
         * 检测节目加载的资源地址是否合法
         */
        override fun onLoadResource(view: WebView?, url: String?) {
            if (isValid(url)){
                Log.d("PlayerWebViewClient", "onLoadResource success:$url")
                super.onLoadResource(view, url)
            }
            else{
                LoggerHandler.playLog.e(mapOf<String, String>(Pair("msg","PlayerWebViewClient onLoadResource"),Pair("url",url?:"")))
            }
        }

        /**
         * 遇到页面错误时进行记录
         */
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            LoggerHandler.playLog.e(mapOf<String, String>(
                    Pair("msg","PlayerWebViewClient onReceivedError"),
                    Pair("errorCode",errorCode.toString()),
                    Pair("description",description?:""),
                    Pair("failingUrl",failingUrl?:"")
            ))
        }
    }

    private inner class PlayerWebChromeClient(val context: Context, val player: PlayerModel): WebChromeClient(){
        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            result?.confirm();
            return true;
        }

        override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return super.onJsConfirm(view, url, message, result)
        }
    }
}