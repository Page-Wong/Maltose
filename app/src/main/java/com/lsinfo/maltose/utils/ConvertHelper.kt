package com.lsinfo.maltose.utils

import com.google.gson.Gson
import java.io.*
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy


/**
 * Created by G on 2018-03-20.
 */
object ConvertHelper {

    fun mapStringString2mapStringAny(arg: MutableMap<String, String?>):MutableMap<String, Any?>{
        val params = mutableMapOf<String, Any?>()
        arg.iterator().forEach { params[it.key] = it.value }
        return params
    }

    fun file2Byte(file: File): ByteArray? {
        if (!file.exists()) return null
        var buffer: ByteArray? = null
        try {
            val fis = FileInputStream(file)
            val bos = ByteArrayOutputStream()
            val b = ByteArray(1024)
            var n = fis.read(b)
            while (n != -1) {
                bos.write(b, 0, n)
                n = fis.read(b)
            }
            fis.close()
            bos.close()
            buffer = bos.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return buffer
    }

    fun byte2File(buf: ByteArray, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        var file: File? = null
        try {
            val dir = File(filePath)
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs()
            }
            file = File(filePath + File.separator + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(buf)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    val gson : Gson = GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create()
}