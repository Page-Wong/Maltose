package com.lsinfo.maltose.model

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import com.lsinfo.maltose.Config
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.*

/**
 * Created by G on 2018-04-28.
 */
class AlarmModelTest {
    private val appContext = InstrumentationRegistry.getTargetContext()
    @Before
    fun setUp() {
        val ed = PreferenceManager.getDefaultSharedPreferences(appContext).edit()
        ed.putString(Config.PREFERENCE_TOKEN_KEY, "token")
        ed.putString(Config.PREFERENCE_DEVICE_ID_KEY, "deviceId")
        ed.commit()
    }
    @Test
    fun getRecentlyDateTime() {
        val item = AlarmModel(
            alarmId = "",
            sign= "",
            time="11:00:00",
            dateSetting="{" +
                    "single:{" +
                    "date:\"2018-04-27,2018-04-28\"" +
                    "}," +
                    "repeat:{" +
                    "dayInWeek:\"1,3,5\"," +
                    "dayInMonth:\"1,10,15,30\"," +
                    "weekInMoth:\"1,3\"," +
                    "monthInYear:\"1,2,7,8\"" +
                    "}" +
                    "}",
                key = "",
                notifyUrl = ""

        )

        val d = item.getRecentlyDateTime()
        assertEquals(d, Date())
    }

}