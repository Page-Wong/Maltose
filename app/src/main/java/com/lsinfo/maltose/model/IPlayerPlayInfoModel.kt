package com.lsinfo.maltose.model

import android.content.Context

/**
 * Created by G on 2018-04-09.
 */
interface IPlayerPlayInfoModel {
    fun save(context: Context): Boolean
    fun delete(context: Context): Boolean
}