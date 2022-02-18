package com.lsinfo.maltose.bean.registration

/**
 * Created by G on 2018-03-16.
 */

enum class RegistrationResultCode{
    NONE,

    SUCCESS,

    DATA_INVALID,

    DATA_URL_NONE,

    DATA_METHOD_INVALID,

    DATA_ARGUMENTS_NONE,

    REGISTRATION_URL_INVALID,

    DATA_MESSAGE_TYPE_INVALID,

    DATA_RESULT_NONE,

    DATA_DEVICE_ID_NONE
}

data class RegistrationResultBean(
        var code: RegistrationResultCode = RegistrationResultCode.NONE,
        var exception: Exception? = null,
        val msg: String = when(code){
            RegistrationResultCode.NONE -> "无"
            RegistrationResultCode.SUCCESS -> "成功"
            RegistrationResultCode.DATA_INVALID -> "数据格式有误"
            RegistrationResultCode.DATA_URL_NONE -> "URL为空"
            RegistrationResultCode.DATA_METHOD_INVALID -> "操作指令有误"
            RegistrationResultCode.DATA_ARGUMENTS_NONE -> "参数为空"
            RegistrationResultCode.REGISTRATION_URL_INVALID -> "设备注册的URL地址有误"
            RegistrationResultCode.DATA_MESSAGE_TYPE_INVALID -> "信息类型有误"
            RegistrationResultCode.DATA_RESULT_NONE -> "数据结果为空"
            RegistrationResultCode.DATA_DEVICE_ID_NONE -> "设备ID为空"
        }
){
}