package com.lsinfo.maltose.model

import android.content.Context
import com.google.gson.Gson
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.Config.OPERATION_METHOD_CLASS
import com.lsinfo.maltose.bean.*
import com.lsinfo.maltose.db.InstructionDbManager
import com.lsinfo.maltose.db.OperationDbManager
import com.lsinfo.maltose.db.OperationDictionaryDbManager
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.utils.FileUtils
import com.lsinfo.maltose.utils.SecurityUtils
import java.io.File
import java.util.*


/**
 * Created by G on 2018-04-09.
 */

enum class OperateType{
    SETTING,//操作设备硬件类的设置
    PLAYING,//控制设备播放
    COMMUNICATION//与服务器通讯
}

enum class OperationStatus{
    PREPARING,//未开始
    RUNNING,//处理中
    SUCCESS,//处理成功
    FAIL//处理失败
}
class OperationModel(
        var operationId: String,//该操作唯一ID
        var instructionId: String,//指令ID
        var type: OperateType,//操作类型
        var startTime: Date,//操作开始时间
        var finishTime: Date? = null,//操作结束时间
        var key: String,//指令关键字
        var method: String,//调用的方法
        var params: SortedMap<String, String?>? = null,//操作方法参数，JSON对象
        var content: SortedMap<String, String?>? = null,//其他参数，JSON对象
        var result: ResultBean? = null,//操作结果
        var status: OperationStatus//操作状态
): IOperationModel {

    override fun save(context: Context): Boolean {
        return if (OperationDbManager.get(context, operationId) == null){
            OperationDbManager.insert(context, this) > 0
        }
        else{
            OperationDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return OperationDbManager.delete(context, operationId) > 0
    }


    override fun isVaild(): Boolean{
        return true
    }

    override fun execute(context: Context): ResultBean {
        var result = ResultBean()
        try {
            //region 检查操作是否有误
            if (!isVaild()) {
                result.code = ResultCode.OPERATION_INVALID
                return result
            }
            //endregion

            //通过完整的类型路径获取类
            val operationClass = Class.forName(OPERATION_METHOD_CLASS)
            if (operationClass == null){
                result.code = ResultCode.OPERATION_METHOD_CLASS_NOT_FOUND
                return result
            }

            //使用newInstance创建对象
            val invoke = operationClass.getConstructor(Context::class.java).newInstance(context)
            if (invoke == null){
                result.code = ResultCode.OPERATION_METHOD_CLASS_NEW_INSTANCE_NONE
                return result
            }

            //获取对象类的方法
            val method = operationClass.getMethod(
                    method,
                    MutableMap::class.java
            )
            if (method == null){
                result.code = ResultCode.OPERATION_METHOD_CLASS_METHOD_NONE
                return result
            }
            val methodParams = params?: mutableMapOf<String, String?>()
            methodParams["instructionId"] = instructionId
            methodParams["operationId"] = operationId

            result = method.invoke(invoke, methodParams) as ResultBean

        }
        catch (e: Exception){
            result.code = ResultCode.OPERATE_ERROR
            result.exception = e
        }
        finally {
            when (result.code){
                ResultCode.SUCCESS -> change2Complete(context, result)
                ResultCode.RUNNING -> change2Running(context, result)
                else -> change2Fail(context, result)
            }
        }
        return result
    }

    override fun change2Running(context: Context, result: ResultBean){
        status = OperationStatus.RUNNING
        save(context)
    }

    override fun change2Complete(context: Context, result: ResultBean){
        status = OperationStatus.SUCCESS
        save(context)
    }

    override fun change2Fail(context: Context, result: ResultBean){
        status = OperationStatus.FAIL
        save(context)
    }
}