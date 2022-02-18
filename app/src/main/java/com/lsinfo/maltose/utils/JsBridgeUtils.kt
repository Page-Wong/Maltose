package com.lsinfo.maltose.utils

import android.util.Log
import android.webkit.JavascriptInterface

/**
 * Created by G on 2018-04-20.
 */
class JsBridgeUtils {
    @JavascriptInterface
    fun test(s: String){
        Log.d("JsBridgeUtils", "test s=$s")
    }
}