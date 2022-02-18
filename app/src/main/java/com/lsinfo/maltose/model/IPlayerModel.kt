package com.lsinfo.maltose.model

import android.content.Context
import com.lsinfo.maltose.bean.ResultBean

/**
 * Created by G on 2018-04-09.
 */
interface IPlayerModel {
    fun save(context: Context): Boolean
    fun delete(context: Context): Boolean
    fun play(context: Context, playInfo: PlayInfoModel?): ResultBean
    fun playNext(context: Context): ResultBean
    fun stop(context: Context, result: ResultBean): ResultBean
    fun startLoop(context: Context): ResultBean
    fun stopLoop(context: Context): ResultBean
    fun skip(context: Context, result: ResultBean): ResultBean
    fun getNextPlayInfo(context: Context): PlayInfoModel?
    fun isPlayInfoReady(context: Context): Boolean
    fun getPlayInfoList(context: Context): MutableList<PlayInfoModel>
    fun changeStatus(context: Context, status: PlayerStatus): ResultBean
    fun isReady():Boolean

}