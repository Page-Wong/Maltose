package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.bean.ResultBean

/**
 * Created by G on 2018-04-27.
 */
interface IOperationModel {
    fun save(context: Context): Boolean
    fun delete(context: Context): Boolean
    fun isVaild(): Boolean
    fun execute(context: Context): ResultBean
    fun change2Running(context: Context, result: ResultBean)
    fun change2Complete(context: Context, result: ResultBean)
    fun change2Fail(context: Context, result: ResultBean)
}