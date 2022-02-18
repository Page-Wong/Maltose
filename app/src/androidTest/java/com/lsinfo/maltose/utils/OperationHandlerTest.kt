package com.lsinfo.maltose.utils

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import com.hzy.lib7z.Z7Extractor
import com.lsinfo.maltose.Config
import org.junit.Test

import org.junit.Before

/**
 * Created by G on 2018-04-13.
 */
class OperationHandlerTest {

    private val appContext = InstrumentationRegistry.getTargetContext()
    @Before
    fun setUp() {
        val ed = PreferenceManager.getDefaultSharedPreferences(appContext).edit()
        ed.putString(Config.PREFERENCE_TOKEN_KEY, "token")
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, "deviceId")
        ed.commit()
    }

    @Test
    fun synPlayInfoList() {
        OperationHandler(appContext).syncPlayInfoList((mutableMapOf()))
        assert(true)
    }



    @Test
    fun extractFile() {
        Z7Extractor.extractFile("${Config.APP_TEMP_PATH}/2","${Config.APP_TEMP_PATH}/22", null)
    }

}