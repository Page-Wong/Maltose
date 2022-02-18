package com.lsinfo.maltose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lsinfo.maltose.utils.DeviceSettingUtils
import com.lsinfo.maltose.utils.WsConnector

/**
 * Created by G on 2018-07-13.
 */
class ConnectionChangeReceiver: BroadcastReceiver() {
    val ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION && DeviceSettingUtils.isNetworkConnected(context)) {
            WsConnector.reconnect()
        }
    }
}