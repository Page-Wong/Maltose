package com.lsinfo.maltose.bean

import com.lsinfo.maltose.model.OperateType
/**
 * Created by G on 2018-03-20.
 */
data class OperationDictionaryBean(
        var key: String,//指令关键字
        var type: OperateType,//操作类型
        var method: String//调用的方法
)


