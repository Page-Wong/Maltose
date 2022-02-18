package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.*
import com.lsinfo.maltose.db.InstructionDbManager
import com.lsinfo.maltose.db.OperationDictionaryDbManager
import com.lsinfo.maltose.utils.ConvertHelper
import com.lsinfo.maltose.utils.HttpHandler
import com.lsinfo.maltose.utils.SecurityUtils
import java.util.*


/**
 * Created by G on 2018-04-09.
 */

enum class InstructionStatus{
    PREPARING,//准备中，未开始
    RUNNING,//处理中
    COMPLETE,//处理完成，完成结果在result中查看
    FEEDBACK//完成向服务器反馈执行结果的操作，整个指令执行完结
}
class InstructionModel(
        var original:String,//原始指令字符串
        var instructionId: String,//指令唯一ID
        var notifyUrl: String?,//通知服务器执行完指令的地址
        var key: String,//指令关键字
        var sign: String,//服务器签名
        var timestamp: Long,//指令时间
        var params: SortedMap<String, String?>? = null,//指令参数，JSON对象
        var content: SortedMap<String, String?>? = null,//其他参数，JSON对象
        var result: ResultBean = ResultBean(),//操作结果
        var status: InstructionStatus = InstructionStatus.PREPARING,//指令状态
        var token: String = ""
): IInstructionModel {
    companion object {
        fun loadFromJson(json: String, result: ResultBean): InstructionModel?{
            return try {
                val item = ConvertHelper.gson.fromJson(json, InstructionModel::class.java)
                item.original = json
                item.token = Config.TOKEN
                result.code = ResultCode.SUCCESS
                item
            }
            catch (e: Exception){
                result.code = ResultCode.INSTRUCTION_SOURCE_CONVERT_ERROR
                result.exception = e
                null
            }
        }
    }

    override fun save(context: Context): Boolean {
        return if (InstructionDbManager.get(context, instructionId) == null){
            InstructionDbManager.insert(context, this) > 0
        }
        else{
            InstructionDbManager.update(context, this) > 0
        }
    }

    override fun delete(context: Context): Boolean {
        return InstructionDbManager.delete(context, instructionId) > 0
    }

    override fun toValidatorString(): String{
        return try {
            var jsonStr = ConvertHelper.gson.toJson(this)
            var dataMap = ConvertHelper.gson.fromJson(jsonStr, SortedMap::class.java)
            /*dataMap.remove("sign")
            ConvertHelper.gson.toJson(dataMap).replace("\\\"","").replace("\"","")*/
            dataMap.remove("original")
            dataMap.remove("result")
            dataMap.remove("status")
            SecurityUtils.sign(dataMap)
        }
        catch (e: Exception){
            String()
        }
    }

    override fun isValid(): Boolean{
        //获取排除sign后的数据串
        var md5String = this.toValidatorString()
        //校验md5码与指令的签名是否相同
        return md5String.isNotEmpty() && this.sign.isNotEmpty() && md5String == this.sign
    }

    override fun resolveOperation(context: Context, result: ResultBean): OperationModel? {
        //region 检查指令是否有误
        if (!isValid()) {
            result.code = ResultCode.INSTRUCTION_INVALID
            return null
        }
        //endregion

        //获取指令对应的操作数据字典
        var operationDictionary = OperationDictionaryDbManager.get(context, key)
        if (operationDictionary == null) {
            result.code = ResultCode.OPERATION_DICTIONARY_NOT_FOUND
            return null
        }

        //组装OperationBean对象
        val item = OperationModel(
                operationId = UUID.randomUUID().toString(),
                instructionId = instructionId,
                startTime = Date(),
                finishTime = null,
                key = operationDictionary.key,
                type = operationDictionary.type,
                method = operationDictionary.method,
                params = params,
                content = content,
                result = null,
                status = OperationStatus.PREPARING
        )
        result.code = ResultCode.SUCCESS
        return item
    }

    override fun execute(context: Context): ResultBean {
        var result = ResultBean()
        try {
            var operation = resolveOperation(context, result)
            if (operation == null || !result.isSuccess()){
                change2Complete(context, result)
            }
            else{
                change2Preparing(context, result)
                result = operation.execute(context)
            }
        }
        catch (e: Exception){
            result.code = ResultCode.INSTRUCTION_EXECUTE_ERROR
            result.exception = e
        }
        finally {
            when (result.code){
                ResultCode.RUNNING -> change2Running(context, result)
                else -> change2Complete(context, result)
            }
            HttpHandler.instructionNotifyApi(context, this, result)
        }
        return result
    }

    override fun change2Preparing(context: Context, result: ResultBean) {
        status = InstructionStatus.PREPARING
        this.result = result
        save(context)
    }

    override fun change2Running(context: Context, result: ResultBean) {
        status = InstructionStatus.RUNNING
        this.result = result
        save(context)
    }

    override fun change2Complete(context: Context, result: ResultBean) {
        status = InstructionStatus.COMPLETE
        this.result = result
        save(context)
    }

    override fun change2Feedback(context: Context, result: ResultBean) {
        status = InstructionStatus.FEEDBACK
        this.result = result
        save(context)
    }
}