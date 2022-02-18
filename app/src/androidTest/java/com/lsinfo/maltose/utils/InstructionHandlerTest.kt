package com.lsinfo.maltose.utils

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.InstructionDbManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Created by G on 2018-04-11.
 */

@RunWith(AndroidJUnit4::class)
class InstructionHandlerTest {

    private val appContext = InstrumentationRegistry.getTargetContext()
    @Before
    fun setUp() {
        val ed = PreferenceManager.getDefaultSharedPreferences(appContext).edit()
        ed.putString(Config.PREFERENCE_TOKEN_KEY, "token")
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, "deviceId")
        ed.commit()
    }

    @Test
    fun execute() {
        InstructionDbManager.deleteAll(appContext, true)
        /*val instructionHandler = InstructionHandler(appContext, "{instructionId=\"instructionId\", deviceId=\"deviceId\", notifyUrl=\"http://192.168.0.7:49469//Home/Test\", key=\"key\", sign=\"5d4a1c30794f9d6ba9fda0696461a94d\", timestamp=123, version=\"1.0\", params={arg=\"aaaaaa\"}, content={}}")
        instructionHandler.startAlarm()
        assertEquals(ResultCode.SUCCESS, instructionHandler.result.code)*/
    }

}