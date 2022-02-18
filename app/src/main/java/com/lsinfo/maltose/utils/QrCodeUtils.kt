package com.lsinfo.maltose.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

/**
 * Created by G on 2018-04-04.
 */
object QrCodeUtils {
    fun createQrCodeBitmap(str: String?, width: Int, height: Int): Bitmap? {
        try {
            //判断URL合法性
            if (str == null || "" == str || str.isEmpty()) {
                return null
            }
            val hints = Hashtable<EncodeHintType, String>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            //图像数据转换，使用了矩阵转换
            val bitMatrix = QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, width, height, hints)
            val pixels = IntArray(width * height)
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = -0x1000000
                    } else {
                        pixels[y * width + x] = -0x1
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
            /*//显示到一个ImageView上面
            sweepIV.setImageBitmap(bitmap);*/
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }
}