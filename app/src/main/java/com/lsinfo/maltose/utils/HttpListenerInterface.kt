package com.lsinfo.maltose.utils

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by G on 2018-04-02.
 */
abstract class HttpListenerInterface(val context: Context) : VolleyListenerInterface(context){

    var result = ResultBean()

    override fun onMyError(e: VolleyError) {
        LoggerHandler.crashLog.e(e.networkResponse?.allHeaders?.map { "${it.name},${it.value}" }?.map { it })
        onError(e)
    }

    override fun onMySuccess(json: String) {
        LoggerHandler.commLog.i(mapOf<String, String>(
                Pair("json", json)
        ))
        try {
            val obj = JSONObject(json)
            if (obj.optInt("code") == 1) {
                try{
                    onSuccess(obj)
                }
                catch (e:Exception){
                    LoggerHandler.crashLog.e(e)
                    result.code = ResultCode.OPERATE_ERROR
                    result.exception = e
                }
            } else {
                onFail(obj)
            }
        }
        catch (e: Exception){
            LoggerHandler.crashLog.e(e)
            result.code = ResultCode.HTTP_ERROR
            result.exception = e
        }
        finally {
            LoggerHandler.commLog.i(mapOf<String, String>(
                    Pair("msg", "HttpListenerInterface onCallBack"),
                    Pair("result", result.toString())
            ))
            onCallBack(result)
        }
    }

    open fun onSuccess(json: JSONObject) {}

    open fun onFail(json: JSONObject){
    }

    open fun onError(error: VolleyError){
        result.code = ResultCode.OPERATE_ERROR
        result.exception = error
        onCallBack(result)
    }

    open fun onCallBack(result: ResultBean){
    }
}