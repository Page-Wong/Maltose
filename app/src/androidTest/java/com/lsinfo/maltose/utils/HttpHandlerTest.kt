package com.lsinfo.maltose.utils

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import com.lsinfo.maltose.Config
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Created by G on 2018-04-18.
 */
class HttpHandlerTest {
    private val appContext = InstrumentationRegistry.getTargetContext()
    @Before
    fun setUp() {
        val ed = PreferenceManager.getDefaultSharedPreferences(appContext).edit()
        ed.putString(Config.PREFERENCE_TOKEN_KEY, "token")
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, "deviceId")
        ed.commit()
    }

    @Test
    fun synPlayInfoResourcesList() {
        /*val obj = JSONObject()
        obj.put("success","true")
        obj.put("dataList",JSONArray(arrayOf(
                JSONObject(mapOf(Pair("playInfoId","1"),Pair("fileMd5","6d7a24843129f1f2d30777b12721e0c1"))),
                JSONObject(mapOf(Pair("playInfoId","2"),Pair("fileMd5","f434f5fef0d1092d752935fb5777f903")))
        )
        ))
        HttpHandler.synPlayInfoResourcesList(appContext, obj)*/
        assert(true)
    }

}