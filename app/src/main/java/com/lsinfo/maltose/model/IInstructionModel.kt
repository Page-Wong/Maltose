package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.bean.ResultBean

/**
 * Created by G on 2018-04-27.
 */
interface IInstructionModel {
    fun save(context: Context): Boolean
    fun delete(context: Context): Boolean
    fun toValidatorString(): String
    fun isValid(): Boolean
    fun resolveOperation(context: Context, result: ResultBean): OperationModel?
    fun execute(context: Context): ResultBean

    fun change2Preparing(context: Context, result: ResultBean)
    fun change2Running(context: Context, result: ResultBean)
    fun change2Complete(context: Context, result: ResultBean)
    fun change2Feedback(context: Context, result: ResultBean)
}