package com.lsinfo.maltose.utils

import android.content.Context
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.VolleyError
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by G on 2018-04-02.
 */
abstract class VolleyListenerInterface(var mContext: Context) {

    // 请求成功时的回调函数
    abstract fun onMySuccess(result: String)

    // 请求失败时的回调函数
    abstract fun onMyError(error: VolleyError)

    // 创建请求的事件监听
    fun responseListener(): Response.Listener<String> {
        return Response.Listener<String> { s -> onMySuccess(s) }
    }

    // 创建请求失败的事件监听
    fun errorListener(): Response.ErrorListener {
        return Response.ErrorListener { volleyError -> onMyError(volleyError) }
    }

}

