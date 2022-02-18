package com.lsinfo.maltose.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.model.PlayInfoStatus
import org.apache.commons.validator.routines.UrlValidator
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.Key
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec


/**
 * Created by G on 2018-03-30.
 */
object SecurityUtils {

//region MD5加密
    var hexdigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    /**
     * 把byte[]数组转换成十六进制字符串表示形式
     * @param tmp    要转换的byte[]
     * @return 十六进制字符串表示形式
     */
    private fun byteToHexString(tmp: ByteArray): String {
        val s: String
        // 用字节表示就是 16 个字节
        val str = CharArray(16 * 2) // 每个字节用 16 进制表示的话，使用两个字符，
        // 所以表示成 16 进制需要 32 个字符
        var k = 0 // 表示转换结果中对应的字符位置
        for (i in 0..15) { // 从第一个字节开始，对 MD5 的每一个字节
            // 转换成 16 进制字符的转换
            val byte0 = tmp[i] // 取第 i 个字节
            str[k++] = hexdigits[byte0.toInt().ushr(4) and 0xf] // 取字节中高 4 位的数字转换,
            // >>> 为逻辑右移，将符号位一起右移
            str[k++] = hexdigits[byte0.toInt() and 0xf] // 取字节中低 4 位的数字转换
        }
        s = String(str) // 换后的结果转换为字符串
        return s
    }

    /**
     * 使用系统的唯一ID获取加密串
     */
    fun md5Encrypt(string: String): String {
        //将注册设备时的唯一ID作为盐加密
        return md5Encrypt(string, Config.DEVICE_ID)
    }

    /**
     * 字符串加盐生成MD5码
     */
    private fun md5Encrypt(string: String, slat: String): String {
        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest:ByteArray = instance.digest((string + slat).toByteArray())
            var sb = StringBuffer()
            for (b in digest) {
                //获取低八位有效值
                var i :Int = b.toInt() and 0xff
                //将整数转化为16进制
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    //如果是一位的话，补0
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * MD5文件加密
     */
    private fun md5Encrypt(file: File): String {
        val bufferSize = 256 * 1024
        var md5 = ""
        var fis: FileInputStream? = null
        val buffer = ByteArray(bufferSize)
        var length: Int = -1
        var md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
            fis = FileInputStream(file)
            while (true) {
                length = fis.read(buffer)
                if (length == -1) {
                    break
                } else {
                    md.update(buffer, 0, length)
                }
            }
            /*md5 = BigInteger(1, md.digest()).toString(16)
            return String.format("%\"0\"32s", md5)*/
            val b = md.digest()
            return byteToHexString(b)
        } catch (e: NoSuchAlgorithmException) {

        } catch (e: FileNotFoundException) {

        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {

            }
        }
        return ""
    }

    /**
     * 获取批量文件的MD5
     */
    fun md5Encrypt(file: ArrayList<File>): String{
        var md5 = String()
        file.forEach { item -> md5 += md5Encrypt(item) }
        return md5Encrypt(md5,"")
    }

    /**
     * MD5文件加盐加密
     */
    fun md5EncryptWithSlat(file: File): String {
        var md5 = md5Encrypt(file)
        if(!md5.isNullOrEmpty()){
            md5 = md5Encrypt(md5)
        }
        return md5
    }

    /**
     * 将map对象转为字符串
     */
    fun getDataString(item: MutableMap<String, Any?>): String{
            return try {
                var dataMap = item.toSortedMap()
                dataMap.remove("sign")
                dataMap.toString()
            } catch (e: Exception){
                String()
            }
        }
//endregion

//region AES加密解密
    private val HEX = "0123456789ABCDEF"
    private val CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding"//AES是加密方式 CBC是工作模式 PKCS5Padding是填充模式
    private val AES = "AES"//AES 加密
    private val SHA1PRNG = "SHA1PRNG"//// SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法

    //二进制转字符
    fun toHex(buf: ByteArray?): String {
        if (buf == null)
            return ""
        val result = StringBuffer(2 * buf.size)
        for (i in buf.indices) {
            appendHex(result, buf[i])
        }
        return result.toString()
    }

    private fun appendHex(sb: StringBuffer, b: Byte) {
        sb.append(HEX[(b.toInt() shr 4) and 0x0f]).append(HEX[b.toInt() and 0x0f])
    }

    // 对密钥进行处理
    @Throws(Exception::class)
    private fun getRawKey(seed: ByteArray): ByteArray {
        val kgen = KeyGenerator.getInstance(AES)
        //for android
        var sr: SecureRandom? = null
        // 在4.2以上版本中，SecureRandom获取方式发生了改变
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            sr = SecureRandom.getInstance(SHA1PRNG, "Crypto")
        } else {
            sr = SecureRandom.getInstance(SHA1PRNG)
        }
        // for Java
        // secureRandom = SecureRandom.getInstance(SHA1PRNG);
        sr!!.setSeed(seed)
        kgen.init(128, sr) //256 bits or 128 bits,192bits
        //AES中128位密钥版本有10个加密循环，192比特密钥版本有12个加密循环，256比特密钥版本则有14个加密循环。
        val skey = kgen.generateKey()
        return skey.encoded
    }

    /*
     * 加密
     */
    fun aesEncrypt(key: String, cleartext: String): String? {
        if (TextUtils.isEmpty(cleartext)) {
            return cleartext
        }
        try {
            val result = aesEncrypt(key, cleartext.toByteArray())
            return Base64.encodeToString(result, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /*
    * 加密
    */
    @Throws(Exception::class)
    private fun aesEncrypt(key: String, clear: ByteArray): ByteArray {
        val raw = getRawKey(key.toByteArray())
        val skeySpec = SecretKeySpec(raw, AES)
        val cipher = Cipher.getInstance(CBC_PKCS5_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(clear)
    }

    /*
     * 解密
     */
    fun aesDecrypt(key: String, encrypted: String): String? {
        if (TextUtils.isEmpty(encrypted)) {
            return encrypted
        }
        try {
            val enc = Base64.decode(encrypted, Base64.DEFAULT)
            val result = aesDecrypt(key, enc)
            return String(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /*
     * 解密
     */
    @Throws(Exception::class)
    private fun aesDecrypt(key: String, encrypted: ByteArray): ByteArray {
        val raw = getRawKey(key.toByteArray())
        val skeySpec = SecretKeySpec(raw, AES)
        val cipher = Cipher.getInstance(CBC_PKCS5_PADDING)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(encrypted)
    }
//endregion

    //region 3DES
    /**
     * ECB加密,不要IV
     * @param key 密钥
     * @param data 明文
     * @return Base64编码的密文
     * @throws Exception
     */
    fun des3EncodeECB(keyStr: String, dataStr: String): String {
        var key = Base64.decode(keyStr, Base64.DEFAULT)
        var data = dataStr.toByteArray(Charsets.UTF_8)
        try {
            var deskey: Key? = null
            val spec = DESedeKeySpec(key)
            val keyfactory = SecretKeyFactory.getInstance("desede")
            deskey = keyfactory.generateSecret(spec)
            val cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, deskey)
            var bOut =  cipher.doFinal(data)
            return String(Base64.encode(bOut, Base64.DEFAULT), Charsets.UTF_8)
        }catch (e: Exception){
            Log.d("", e.message)
            return ""
        }
    }

    /**
     * ECB加密,不要IV
     * @param key 密钥
     * @param data 明文
     * @return Base64编码的密文
     * @throws Exception
     */
    @Throws(Exception::class)
    fun des3EncodeECB(key: ByteArray, data: ByteArray): ByteArray {
        try {
            var deskey: Key? = null
            val spec = DESedeKeySpec(key)
            val keyfactory = SecretKeyFactory.getInstance("desede")
            deskey = keyfactory.generateSecret(spec)
            val cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, deskey)
            return cipher.doFinal(data)
        }catch (e: Exception){
            Log.d("", e.message)
            return ByteArray(0)
        }
    }

    /**
     * ECB解密,不要IV
     * @param key 密钥
     * @param data Base64编码的密文
     * @return 明文
     * @throws Exception
     */
    @Throws(Exception::class)
    fun des3DecodeECB(key: ByteArray, data: ByteArray): ByteArray {
        var deskey: Key? = null
        val spec = DESedeKeySpec(key)
        val keyfactory = SecretKeyFactory.getInstance("desede")
        deskey = keyfactory.generateSecret(spec)
        val cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, deskey)
        return cipher.doFinal(data)
    }

    /**
     * CBC加密
     * @param key 密钥
     * @param keyiv IV
     * @param data 明文
     * @return Base64编码的密文
     * @throws Exception
     */
    @Throws(Exception::class)
    fun des3EncodeCBC(key: ByteArray, keyiv: ByteArray, data: ByteArray): ByteArray {
        var deskey: Key? = null
        val spec = DESedeKeySpec(key)
        val keyfactory = SecretKeyFactory.getInstance("desede")
        deskey = keyfactory.generateSecret(spec)
        val cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding")
        val ips = IvParameterSpec(keyiv)
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips)
        val bOut = cipher.doFinal(data)
        return cipher.doFinal(data)
    }

    /**
     * CBC解密
     * @param key 密钥
     * @param keyiv IV
     * @param data Base64编码的密文
     * @return 明文
     * @throws Exception
     */
    @Throws(Exception::class)
    fun des3DecodeCBC(key: ByteArray, keyiv: ByteArray, data: ByteArray): ByteArray {
        var deskey: Key? = null
        val spec = DESedeKeySpec(key)
        val keyfactory = SecretKeyFactory.getInstance("desede")
        deskey = keyfactory.generateSecret(spec)
        val cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding")
        val ips = IvParameterSpec(keyiv)
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips)
        return cipher.doFinal(data)
    }
    //endregion

    /**
     * 判断url是否合法
     * @param url url
     * @return url是否合法
     */
    fun isUrlValid(url: String): Boolean {
        val schemas = arrayOf("http", "https")
        val urlValidator = UrlValidator(schemas)
        if (!urlValidator.isValid(url)) return false
        return URL(url).host == URL(Config.HTTP_HOST).host
    }

    /**
     * 文件检查列表
     */
    private var checkingList: java.util.ArrayList<String> = arrayListOf()

    /**
     * 检查节目文件夹内容是否与原始内容一致
     */
    fun checkPlayInfo(context: Context, playInfoId: String, isDownload: Boolean = false){
        if (!checkingList.contains(playInfoId)) {
            checkingList.add(playInfoId)
            try {
                val item = PlayInfoDbManager.get(context, playInfoId)
                if (item != null){
                    //校验路径文件是否合法
                    val valid = SecurityUtils.isPathFilesValid(item.absolutePath, item.fileMd5)
                    //更新节目状态
                    item.status = if (valid) {
                        PlayInfoStatus.VALID
                    } else {
                        PlayInfoStatus.INVALID
                    }
                    //PlayInfoDbManager.update(context, item)
                    item.save(context)

                    //如果校验失败，则跳过此节目，并重新下载资源文件
                    if (!valid) {
                        PlayerListManager.playerList.forEach {
                            if (it.playInfo?.playInfoId == playInfoId) {
                                it.skip(context, ResultBean(ResultCode.PLAY_INFO_INVALID))
                            }
                        }
                        if (isDownload) DownloadUtils.downloadResources(context, playInfoId)
                    } else {
                        PlayerListManager.startAllLoop(context)
                    }
                }

            } finally {
                checkingList.remove(playInfoId)
            }
        }
    }

    /**
     * 检查所有节目文件夹内容是否与原始内容一致
     */
    fun checkAllPlayInfo(context: Context){
        Thread(Runnable(){
            run(){
                PlayInfoDbManager.getAll(context).forEach { checkPlayInfo(context, it.playInfoId, true) }
            }
        }).start()
    }

    /**
     * 校验路径文件夹的所有文件是否合法
     */
    fun isPathFilesValid(path: String, md5: String): Boolean{
        val files = FileUtils.getAllFiles(File(path), arrayListOf<File>())
        val filesMd5 = SecurityUtils.md5Encrypt(SecurityUtils.md5Encrypt(files))
        LoggerHandler.runLog.d("isPathFilesValid filesMd5:$filesMd5,md5=$md5")
        return filesMd5 == md5
    }

    fun sign(dataMap: SortedMap<*, *>):String{
        dataMap.remove("sign")
        dataMap.remove("operationId")
        val dataString = ConvertHelper.gson.toJson(dataMap).replace("\\\"","").replace("\"","")
        //获取数据串的md5码
        val md5String = SecurityUtils.md5Encrypt(dataString)
        return URLEncoder.encode(md5String, "UTF-8")
    }
}