package com.lsinfo.maltose.utils

import android.util.Log
import java.io.*
import java.nio.charset.Charset


/**
 * Created by G on 2018-04-26.
 */
object ShellUtils {

    @Throws(Exception::class)
    fun terminal(command: String): Boolean {
        var result = false
        var dataOutputStream: DataOutputStream? = null
        var errorStream: BufferedReader? = null
        try {
            // 申请su权限
            val process = Runtime.getRuntime().exec("su")
            dataOutputStream = DataOutputStream(process.outputStream)
            dataOutputStream.write("$command\n".toByteArray(Charset.forName("utf-8")))
            dataOutputStream.flush()
            dataOutputStream.writeBytes("exit\n")
            dataOutputStream.flush()
            process.waitFor()
            errorStream = BufferedReader(InputStreamReader(process.errorStream))
            var msg = ""
            var line = errorStream.readLine()
            // 读取命令的执行结果
            while (line != null) {
                msg += line
                line = errorStream.readLine()
            }
            Log.d("ShellUtils", "terminal： $msg")
            // 如果执行结果中包含Failure字样就认为是执行失败，否则就认为执行成功
            if (!msg.contains("Failure")) {
                result = true
            }
        } catch (e: Exception) {
            Log.e("ShellUtils", e.message, e)
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close()
                }
                if (errorStream != null) {
                    errorStream.close()
                }
            } catch (e: IOException) {
                Log.e("ShellUtils", e.message, e)
            }

        }
        return result
    }
}