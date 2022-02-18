package com.lsinfo.maltose.utils

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.lsinfo.maltose.App
import java.util.HashMap

/**
 * Created by G on 2018-04-02.
 */
open class StringRequestPlus(method: Int, url: String, private val mListener: Response.Listener<String>, errorListener: Response.ErrorListener) : StringRequest(method, url, mListener, errorListener) {

    //重写getHeader()方法,请求会自动调用这个方法来获取请求头部,所以我们将本地的cookie存放进一个map返回回去,cookie就会包含到header里面去
    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        var headers = super.getHeaders()

        if (headers == null || headers == emptyMap<Any, Any>()) {

            headers = hashMapOf()
        }
        //这个方法就是自定义Application类中添加cookie的方法
        App.getInstance().addSessionCookie(headers)
        return headers
    }

    override fun deliverResponse(jsonArray: String) {
        mListener.onResponse(jsonArray)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {

        //在处理返回信息的时候,服务器会返回cookie,在这里截取到cookie并且存储到本地
        if (response != null) App.getInstance().checkSessionCookie(response.headers)
        return super.parseNetworkResponse(response)
    }

/*@Override
public RetryPolicy getRetryPolicy()
{
    RetryPolicy retryPolicy = new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    return retryPolicy;
}*/
}