package com.lsinfo.maltose.bean.registration

import android.util.Log
import com.lsinfo.maltose.bean.WsMessageTypeEnum
import com.lsinfo.maltose.utils.LoggerHandler
import org.json.JSONObject

/**
 * Created by G on 2018-04-08.
 */
data class RegistrationMessageBean(
        var messageType : WsMessageTypeEnum,
        var data: String,
        val dataBean: RegistrationDataBean? = RegistrationDataBean.fromString(data)
){
    companion object {
        fun fromString(string: String): RegistrationMessageBean? {
            try {
                val obj = JSONObject(string)
                return RegistrationMessageBean(
                        messageType = WsMessageTypeEnum.values()[(obj.optInt("messageType"))],
                        data = obj.optString("data")
                )
            }
            catch (e: Exception){
                LoggerHandler.crashLog.e(e)
            }
            return null
        }
    }

    fun isVaild(): RegistrationResultBean{
        var result = RegistrationResultBean(code=RegistrationResultCode.SUCCESS)
        when(messageType){
            WsMessageTypeEnum.ClientMethodInvocation -> {
                if (dataBean == null) {
                    result.code = RegistrationResultCode.DATA_INVALID
                    return result
                }
                if (dataBean.arguments == null){
                    result.code = RegistrationResultCode.DATA_ARGUMENTS_NONE
                    return result
                }
                return dataBean.isVaild()
            }
            WsMessageTypeEnum.ConnectionEvent -> {
                result.code = RegistrationResultCode.SUCCESS
            }
            else -> {
                result.code = RegistrationResultCode.DATA_MESSAGE_TYPE_INVALID

            }
        }
        return result
    }
}