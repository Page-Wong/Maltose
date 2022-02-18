package com.lsinfo.maltose.utils

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.lsinfo.maltose.App

/**
 * Created by G on 2018-04-02.
 */
object VolleyRequestUtils {
    //lateinit var stringRequest: StringRequest
    //lateinit var context: Context

    /*
    * 获取GET请求内容
    * 参数：
    * context：当前上下文；
    * url：请求的url地址；
    * tag：当前请求的标签；
    * volleyListenerInterface：VolleyListenerInterface接口；
    * */
    fun requestGet(context: Context, url: String, tag: String, volleyListenerInterface: VolleyListenerInterface) {
        //this.context = context
        // 清除请求队列中的tag标记请求
        App.getInstance().requestQueue.cancelAll(tag)
        // 创建当前的请求，获取字符串内容
        var stringRequest = StringRequestPlus(Request.Method.GET, url, volleyListenerInterface.responseListener(), volleyListenerInterface.errorListener())
        // 为当前请求添加标记
        stringRequest.tag = tag
        stringRequest.retryPolicy = DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        HttpsTrustManager.allowAllSSL()
        // 将当前请求添加到请求队列中
        App.getInstance().requestQueue.add(stringRequest)
        // 重启当前请求队列
        //LsApplication.getRequestQueue().start();

        LoggerHandler.commLog.i("tag:$tag url:$url")
    }

    /*
    * 获取POST请求内容（请求的代码为Map）
    * 参数：
    * context：当前上下文；
    * url：请求的url地址；
    * tag：当前请求的标签；
    * params：POST请求内容；
    * volleyListenerInterface：VolleyListenerInterface接口；
    * */
    fun requestPost(context: Context, url: String, tag: String, params: Map<String, String?>, volleyListenerInterface: VolleyListenerInterface) {
        //this.context = context
        // 清除请求队列中的tag标记请求
        App.getInstance().requestQueue.cancelAll(tag)
        // 创建当前的POST请求，并将请求内容写入Map中
        var stringRequest = object : StringRequestPlus(Request.Method.POST, url, volleyListenerInterface.responseListener(), volleyListenerInterface.errorListener()) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?>{
                return params
            }
        }
        // 为当前请求添加标记
        stringRequest.tag = tag
        stringRequest.retryPolicy = DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        HttpsTrustManager.allowAllSSL()
        // 将当前请求添加到请求队列中
        App.getInstance().requestQueue.add(stringRequest)
        // 重启当前请求队列
        //LsApplication.getRequestQueue().start();
    }


}