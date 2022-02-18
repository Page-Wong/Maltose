package com.lsinfo.maltose.bean

import com.google.gson.Gson
import com.lsinfo.maltose.utils.ConvertHelper

/**
 * Created by G on 2018-03-16.
 */

enum class ResultCode{
    NONE,

    SUCCESS,

    RUNNING,

    INSTRUCTION_SOURCE_TYPE_INVALID,

    INSTRUCTION_SOURCE_CONVERT_ERROR,

    INSTRUCTION_RESOLVE_NONE,

    INSTRUCTION_INVALID,

    OPERATION_DICTIONARY_NOT_FOUND,

    OPERATION_NONE,

    OPERATION_INVALID,

    OPERATION_METHOD_CLASS_NOT_FOUND,

    OPERATION_METHOD_CLASS_NEW_INSTANCE_NONE,

    OPERATION_METHOD_CLASS_METHOD_NONE,

    OPERATE_ERROR,

    INSTRUCTION_UPDATE_STATUS_FAIL,

    PLAY_INFO_INVALID,

    PLAY_INFO_NONE,

    PLAYER_NONE,

    HTTP_ERROR,

    PLAYER_TYPE_INVALID,

    PARAMS_INVALID,

    PLAYER_UNREADY,

    PLAYER_LOOP_CONTROLLER_NONE,

    PLAYER_PLAY_NEXT_ERROR,

    PLAY_INFO_UNREADY,

    GET_APP_VERSION_ERROR,

    PLAY_INFO_LAUNCHER_NONE,

    PLAYER_STOP_ERROR,

    OPERATION_HANDLER_STOP_PLAY,

    SCREENSHOT_NONE,

    INSTRUCTION_EXECUTE_ERROR,

    WS_MESSAGE_LAUNCHER_NONE,

    WS_MESSAGE_ERROR,

    ACTIVITY_ERROR,

    PLAYER_REMOVE_VIEW
}

data class ResultBean(
        var code: ResultCode = ResultCode.NONE,
        var exception: Exception? = null
){

    var msg: String = ""
        get() = when(code){
            ResultCode.NONE -> "无"
            ResultCode.SUCCESS -> "成功"
            ResultCode.RUNNING -> "处理中"
            ResultCode.INSTRUCTION_SOURCE_TYPE_INVALID -> "指令数据数据类型无效"
            ResultCode.INSTRUCTION_SOURCE_CONVERT_ERROR -> "指令解析出错"
            ResultCode.INSTRUCTION_RESOLVE_NONE -> "解析指令对象为空"
            ResultCode.INSTRUCTION_INVALID -> "指令验证出错"
            ResultCode.OPERATION_DICTIONARY_NOT_FOUND -> "获取操作数据字典为空"
            ResultCode.OPERATION_NONE -> "操作对象为空"
            ResultCode.OPERATION_INVALID -> "对象验证出错"
            ResultCode.OPERATION_METHOD_CLASS_NOT_FOUND -> "不能获取操作Class"
            ResultCode.OPERATION_METHOD_CLASS_NEW_INSTANCE_NONE -> "不能使用默认构造函数获取对象"
            ResultCode.OPERATION_METHOD_CLASS_METHOD_NONE -> "不能获取操作Method"
            ResultCode.OPERATE_ERROR -> "操作失败"
            ResultCode.INSTRUCTION_UPDATE_STATUS_FAIL -> "指令状态更新失败"
            ResultCode.PLAY_INFO_INVALID -> "播放内容不合法"
            ResultCode.PLAY_INFO_NONE -> "播放内容为空"
            ResultCode.PLAYER_NONE -> "播放器未初始化"
            ResultCode.HTTP_ERROR -> "HTTP执行出错"
            ResultCode.PLAYER_TYPE_INVALID -> "播放器类型有误"
            ResultCode.PARAMS_INVALID -> "参数有误"
            ResultCode.PLAYER_UNREADY -> "播放器未准备完成"
            ResultCode.PLAYER_LOOP_CONTROLLER_NONE -> "播放器循环控制器为空"
            ResultCode.PLAYER_PLAY_NEXT_ERROR -> "播放器播放下一个节目出错"
            ResultCode.PLAY_INFO_UNREADY -> "播放内容尚未准备完成"
            ResultCode.GET_APP_VERSION_ERROR -> "获取APP版本号出错"
            ResultCode.PLAY_INFO_LAUNCHER_NONE -> "播放内容启动项不存在"
            ResultCode.PLAYER_STOP_ERROR -> "播放器停止出错"
            ResultCode.OPERATION_HANDLER_STOP_PLAY -> "主动操作停止播放"
            ResultCode.SCREENSHOT_NONE -> "截屏为空"
            ResultCode.INSTRUCTION_EXECUTE_ERROR -> "指令执行出错"
            ResultCode.WS_MESSAGE_LAUNCHER_NONE -> "原始指令为空"
            ResultCode.WS_MESSAGE_ERROR -> "原始指令处理出错"
            ResultCode.ACTIVITY_ERROR -> "当前页面有误"
            ResultCode.PLAYER_REMOVE_VIEW -> "删除播放器"
        }

    override fun toString(): String {
        return ConvertHelper.gson.toJson(this)
    }

    fun fromString(result: String): ResultBean {
        return ConvertHelper.gson.fromJson(result, ResultBean::class.java)
    }

    fun isSuccess(): Boolean = code == ResultCode.SUCCESS
}