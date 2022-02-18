package com.lsinfo.maltose.model

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import com.lsinfo.maltose.db.InstructionDbManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by G on 2018-04-27.
 */
class InstructionModelTest {
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
        var result = ResultBean()
        val instruction = InstructionModel.loadFromJson("{instructionId=\"instructionId\", deviceId=\"deviceId\", notifyUrl=\"http://192.168.0.7:49469//Home/Test\", key=\"key\", sign=\"5d4a1c30794f9d6ba9fda0696461a94d\", timestamp=123, version=\"1.0\", params={arg=\"aaaaaa\"}, content={}}", result)
        result = instruction!!.execute(appContext)
        assertEquals(ResultCode.SUCCESS, result)
    }

}