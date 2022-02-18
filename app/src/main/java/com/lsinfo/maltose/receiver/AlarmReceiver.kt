package com.lsinfo.maltose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.AlarmDbManager
import com.lsinfo.maltose.model.InstructionModel
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.LoggerHandler
import com.lsinfo.maltose.utils.SecurityUtils
import java.util.*

/**
 * Created by G on 2018-03-16.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "EXECUTE_INSTRUCTION") return

        val alarmId = intent.getStringExtra("alarmId")
        val alarm = AlarmDbManager.get(context, alarmId) ?: return
        if (!alarm.isValid()){
            LoggerHandler.alarmLog.w(mapOf<String, String>(
                    Pair("event", "alarm invalid"),
                    Pair("alarm", ConvertHelper.gson.toJson(alarm))
            ))
            return
        }
        val instruction = InstructionModel(
                original = ConvertHelper.gson.toJson(this),
                instructionId = UUID.randomUUID().toString(),
                notifyUrl = alarm.notifyUrl,
                key = alarm.key,
                sign = "",
                timestamp = Date().time,
                params = alarm.params,
                content = alarm.content,
                token = Config.TOKEN
        )
        instruction.sign = SecurityUtils.md5Encrypt(instruction.toValidatorString())
        var result = instruction.execute(context)

        LoggerHandler.alarmLog.w(mapOf<String, String>(
                Pair("event", "alarm run"),
                Pair("result", result.toString()),
                Pair("alarm", ConvertHelper.gson.toJson(alarm))
        ))
        alarm.startAlarm(context)
    }
}