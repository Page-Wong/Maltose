package com.lsinfo.maltose.model

import android.content.Context
import java.util.*

/**
 * Created by G on 2018-04-27.
 */
interface IAlarmModel {
    fun save(context: Context): Boolean
    fun delete(context: Context): Boolean
    fun toValidatorString(): String
    fun isValid(): Boolean
    fun getRecentlyDateTime(): Date?
    fun startAlarm(context: Context)
}