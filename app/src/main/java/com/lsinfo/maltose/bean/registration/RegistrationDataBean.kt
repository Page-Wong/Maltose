package com.lsinfo.maltose.bean.registration

import android.util.Log
import com.google.gson.Gson
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.LoggerHandler
import com.lsinfo.maltose.utils.SecurityUtils
import org.json.JSONObject

/**
 * Created by G on 2018-04-08.
 */
data class RegistrationDataBean(
    var methodName: String,
    var arguments: HashMap<String, Any?>? = null
){
    companion object {
        const val REGISTRATION_URL = "RegistrationUrl"
        const val REGISTRATION_RESULT = "RegistrationResult"

        fun fromString(string: String): RegistrationDataBean?{
            if (string.isEmpty()) return null
            try {
                val data = JSONObject(string)
                var item = RegistrationDataBean(
                        methodName=data.optString("methodName"),
                        arguments = ConvertHelper.gson.fromJson(data.optJSONObject("arguments").toString(), HashMap<String, Any?>()::class.java)
                )
                return item
            }
            catch (e: Exception){
                LoggerHandler.crashLog.e(e)
            }
            return null
        }
    }

    fun isVaild(): RegistrationResultBean{
        var result = RegistrationResultBean(code=RegistrationResultCode.SUCCESS)
        if (arguments == null){
            result.code = RegistrationResultCode.DATA_ARGUMENTS_NONE
            return result
        }
        if (!arguments!!.containsKey("result")){
            result.code = RegistrationResultCode.DATA_RESULT_NONE
            return result
        }
        when(methodName){
            REGISTRATION_URL -> {
                if (arguments!!["result"].toString() == "Success" && !arguments!!.containsKey("url")){
                    result.code = RegistrationResultCode.DATA_URL_NONE
                    return result
                }
                /*if (!SecurityUtils.isUrlValid(arguments!!["url"].toString())) {
                    result.code = RegistrationResultCode.REGISTRATION_URL_INVALID
                    return result
                }*/
            }

            REGISTRATION_RESULT -> {
                if (arguments!!["result"].toString() == "Success" && !arguments!!.containsKey("deviceId")){
                    result.code = RegistrationResultCode.DATA_DEVICE_ID_NONE
                    return result
                }
            }

            else -> {
                result.code = RegistrationResultCode.DATA_METHOD_INVALID
            }
        }
        return result
    }
}