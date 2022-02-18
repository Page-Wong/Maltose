package com.lsinfo.maltose.model

import android.content.Context
import com.google.gson.Gson
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.AlarmDbManager
import com.lsinfo.maltose.utils.SecurityUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.app.PendingIntent
import android.content.Intent
import com.lsinfo.maltose.BuildConfig
import com.lsinfo.maltose.receiver.AlarmReceiver
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.LoggerHandler


/**
 * Created by G on 2018-04-27.
 */
class AlarmModel(
        val alarmId: String,
        var sign: String,//服务器签名
        val time: String,
        /**
         * 参考数据格式：
         * {
                single:{
                    date:"2018-04-27,2018-04-28"
                },
                repeat:{
                    dayInWeek:"1,3,5,7",
                    dayInMonth:"1,10,15,30",
                    weekInMoth:"1,3",
                    monthInYear:"1,2,7,8"
                }
            }
         */
        val dateSetting: String,
        val notifyUrl: String,//通知服务器执行完指令的地址
        val key: String,//指令关键字
        var params: SortedMap<String, String?>? = null,//指令参数，JSON对象
        var content: SortedMap<String, String?>? = null//其他参数，JSON对象
): IAlarmModel {

    override fun save(context: Context): Boolean {
        return if (AlarmDbManager.get(context, alarmId) == null){
            AlarmDbManager.insert(context, this) > 0
        }
        else{
            AlarmDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return AlarmDbManager.delete(context, alarmId) > 0
    }

    override fun toValidatorString(): String{
        return try {
            var jsonStr = ConvertHelper.gson.toJson(this)
            var dataMap = ConvertHelper.gson.fromJson(jsonStr, SortedMap::class.java)
            /*dataMap.remove("sign")
            dataMap.toString().replace("\\\"","").replace("\"","")*/
            SecurityUtils.sign(dataMap)
        }
        catch (e: Exception){
            String()
        }
    }

    override fun isValid(): Boolean{
        //TODO G 用于测试，正式使用时删除
        if (BuildConfig.DEBUG) return true
        if (getRecentlyDateTime() == null) return false
        //获取排除sign后的数据串
        var md5String = this.toValidatorString()
        //校验md5码与指令的签名是否相同
        return md5String.isNotEmpty() && this.sign.isNotEmpty() && md5String == this.sign
    }

    override fun getRecentlyDateTime(): Date? {
        var date: Date? = null
        var calendar = Calendar.getInstance()

        val isSkipToday = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).parse("${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)} $time").time < calendar.time.time
        val isSkipThisWeek = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && isSkipToday
        val isSkipThisMonth = calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH) && isSkipToday
        try {

            val setting = JSONObject(dateSetting)
            val single = setting.getJSONObject("single")
            val repeat = setting.getJSONObject("repeat")

            var recentSingleDate: Date? = null
            if (single != null && single.has("date")){
                val dateList = single["date"].toString().split(",")
                dateList.forEach {
                    val d = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).parse("$it $time")
                    if (d > Date() && (recentSingleDate == null || recentSingleDate!!.time > d.time )){
                        recentSingleDate = d
                    }
                }
            }

            var recentRepeatDate: Date? = null
            if (repeat != null && (repeat.has("dayInWeek") || repeat.has("dayInMonth") || repeat.has("weekInMoth") || repeat.has("monthInYear"))){
                val dayInWeekList = if (repeat.has("dayInWeek")) repeat["dayInWeek"].toString().split(",").map { it.toInt() } else arrayListOf()
                val dayInMonthList = if (repeat.has("dayInMonth")) repeat["dayInMonth"].toString().split(",").map { it.toInt() } else arrayListOf()
                val weekInMothList = if (repeat.has("weekInMoth")) repeat["weekInMoth"].toString().split(",").map { it.toInt() } else arrayListOf()
                val monthInYearList = if (repeat.has("monthInYear")) repeat["monthInYear"].toString().split(",").map { it.toInt() } else arrayListOf()

                val recentRepeatWeekDate = getRecentlyWeekDay(dayInWeekList, isSkipToday)
                val recentRepeatMonthDate = getRecentlyMonthDay(dayInMonthList, isSkipToday)
                val recentRepeatMonthWeek = getRecentlyMonthWeek(weekInMothList, isSkipThisWeek)
                val recentRepeatYearMonth = getRecentlyYearMonth(monthInYearList, isSkipThisMonth)
                recentRepeatDate = arrayListOf(recentRepeatWeekDate, recentRepeatMonthDate, recentRepeatMonthWeek, recentRepeatYearMonth).minBy {
                    it?.time ?: Long.MAX_VALUE }
            }

            date = arrayListOf(recentRepeatDate, recentSingleDate).minBy { it?.time ?: Long.MAX_VALUE }
            if (date != null){
                date = SimpleDateFormat(Config.DATE_TIME_FORMAT_PATTERN).parse("${SimpleDateFormat(Config.DATE_FORMAT_PATTERN).format(date)} $time")
            }
        }
        catch (e:Exception){
            LoggerHandler.crashLog.e(e)
        }
        return date
    }

    /**
     * 获取最近的星期条件对应的日期
     */
    private fun getRecentlyWeekDay(dayInWeekList: List<Int>, isSkipToday: Boolean): Date?{
        if (dayInWeekList.isEmpty()) return null
        var calendar = Calendar.getInstance()
        //获取本周的第几天
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)-1
        //获取大于或等于今天的星期几
        val newWeekDay = dayInWeekList.filter { if (isSkipToday) it > weekDay else it >= weekDay }.min()
        //如果本周获取不到星期几则返回下周最近的星期几对应的日期
        return if (newWeekDay == null){
            calendar.add(Calendar.DAY_OF_YEAR, dayInWeekList.min()!! - weekDay + 7)
            calendar.time
        }
        //获取推迟N天的日期
        else{
            calendar.add(Calendar.DAY_OF_YEAR, newWeekDay - weekDay)
            calendar.time
        }

    }

    /**
     * 获取最近的月份条件对应的日期
     */
    private fun getRecentlyMonthDay(dayInMonthList: List<Int>, isSkipToday: Boolean): Date?{
        if (dayInMonthList.isEmpty()) return null
        var calendar = Calendar.getInstance()
        //获取本月的第几天
        val monthDay = calendar.get(Calendar.DAY_OF_MONTH)
        //获取大于或等于今天的日期
        val newMonthDay = dayInMonthList.filter { if (isSkipToday) it > monthDay else it >= monthDay }.min()
        //如果本月获取不到日期则返回下月最近的日期
        return if (newMonthDay == null){
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1)
            calendar.set(Calendar.DAY_OF_MONTH, dayInMonthList.min()!!)
            calendar.time
        }
        //获取推迟N天的日期
        else{
            calendar.add(Calendar.DAY_OF_YEAR, newMonthDay - monthDay)
            calendar.time
        }

    }

    /**
     * 获取最近的月份条件对应的周数的最近日期
     */
    private fun getRecentlyMonthWeek(weekInMothList: List<Int>, isSkipThisWeek: Boolean): Date?{
        if (weekInMothList.isEmpty()) return null
        var calendar = Calendar.getInstance()
        //获取本月的第几周
        val monthWeek = calendar.get(Calendar.WEEK_OF_MONTH)
        //获取本月大于或等于本周的周数
        val newMonthWeek = weekInMothList.filter { if (isSkipThisWeek) it > monthWeek else it >= monthWeek }.min()
        //如果本月获取不到周数则返回下月最近的周数的星期一
        return when (newMonthWeek) {
            null -> {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1)
                calendar.set(Calendar.WEEK_OF_MONTH, weekInMothList.min()!!)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.time
            }
        //如果是本周则返回今天
            0 -> calendar.time
        //获取推迟N周的星期一
            else -> {
                calendar.add(Calendar.WEEK_OF_MONTH, newMonthWeek - monthWeek)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.time
            }
        }

    }

    /**
     * 获取最近的年份条件对应的月份的最近日期
     */
    private fun getRecentlyYearMonth(monthInYearList: List<Int>, isSkipThisMonth: Boolean): Date?{
        if (monthInYearList.isEmpty()) return null
        var calendar = Calendar.getInstance()
        //获取本月
        val month = calendar.get(Calendar.MONTH)+1
        //获取本年大于或等于本月的月数
        val newMonth = monthInYearList.filter { if (isSkipThisMonth) it > month else it >= month }.min()
        //如果本年获取不到月数则返回下年最近的月数的第一天
        return when (newMonth) {
            null -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.set(Calendar.MONTH, monthInYearList.min()!! - 1)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
                calendar.time
            }
        //如果是本月则返回今天
            0 -> calendar.time
        //获取推迟N月的第一天
            else -> {
                calendar.add(Calendar.MONTH, newMonth - month)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
                calendar.time
            }
        }

    }

    override fun startAlarm(context: Context) {
        if (isValid()) return

        //操作：发送一个广播
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "EXECUTE_INSTRUCTION"
        intent.putExtra("alarmId", alarmId)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)

        val alarm = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarm.set(AlarmManager.RTC_WAKEUP, getRecentlyDateTime()!!.time, sender)
    }
}