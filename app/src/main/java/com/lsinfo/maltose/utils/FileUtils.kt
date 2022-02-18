package com.lsinfo.maltose.utils

import java.net.MalformedURLException
import java.net.URL
import com.example.yf_rk3288_api.YF_RK3288_API_Manager
import com.lsinfo.maltose.App
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.nio.file.Files.isDirectory
import java.io.File.separator




/**
 * Created by G on 2018-04-10.
 */
object FileUtils {
    tailrec fun getAllFiles(path: File, fileList: ArrayList<File>):ArrayList<File>{
        if (path.isDirectory){
            path.listFiles()?.forEach { item -> getAllFiles(item, fileList) }
        }
        //如果是文件则直接加入
        else{
            fileList.add(path)
        }
        return fileList
    }

    /*tailrec fun getAllPlayFiles(path: File, fileList: ArrayList<File>):ArrayList<File>{
        if (path.isDirectory){
            val files = path.listFiles()
            files.forEach { item -> getAllPlayFiles(item, fileList) }
        }
        //如果是文件则直接加入
        else if (isPlayFileType(getFileType(path.launcher))){
            fileList.add(path)
        }
        return fileList
    }

    fun isPlayFileType(fileType: String?): Boolean{
        var success = false
        Config.PLAY_FILE_TYPE.forEach { item ->
            if (fileType == item) {
                success = true
            }
        }
        return success
    }*/


    fun getFileType(fileName: String?): String? {
        return try {
            fileName?.substring(fileName.lastIndexOf(".") + 1, fileName.length)?.toLowerCase();
        }
        catch (e: Exception){
            null
        }
    }

    fun getHtmlString(urlString: String): String{
        val url: URL
        var temp: String?
        val sb = StringBuffer()
        try {
            url = URL(urlString)
            val isr = BufferedReader(InputStreamReader(url.openStream(), "utf-8"))// 读取网页全部内容
            temp = isr.readLine()
            while (temp != null) {
                sb.append(temp)
                temp = isr.readLine()
            }
            isr.close()
        } catch (me: MalformedURLException) {
            println("你输入的URL格式有问题！请仔细输入")
            me.toString()
            throw me
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        return sb.toString()

    }

    fun getSDPath(): String {
        return YF_RK3288_API_Manager(App.getInstance()).yfgetInternalSDPath()
        /*var sdDir: File? = null
        val sdCardExist = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory()//获取跟目录
        }
        return sdDir!!.toString()*/
    }

    fun zipFolder(srcFilePath:String, zipFilePath: String){
// 创建Zip包
        var zipFile = File(zipFilePath)
        if (!zipFile.exists()) zipFile.createNewFile()
        var outZip = ZipOutputStream(FileOutputStream(zipFilePath))
// 打开要输出的文件
        var file = File(srcFilePath)
// 压缩
        try {
            zipFiles(file.parent + File.separator, file.name, outZip)
        }
        catch (e: Exception){

        }
        finally {
// 完成,关闭
            outZip.finish()
            outZip.close()

        }
    }

    fun zipFiles(folderPath: String ,  filePath: String,  zipOut: ZipOutputStream) {
        var file = File(folderPath + filePath)
// 判断是不是文件
        if (file.isFile) {
            var zipEntry = ZipEntry(filePath)
            var inputStream = FileInputStream(file)
            zipOut.putNextEntry(zipEntry);
            var buffer = ByteArray(1024)
            var len = inputStream.read(buffer)
            while (len != -1) {
                zipOut.write(buffer, 0, len);
                len = inputStream.read(buffer)
            }
            inputStream.close();
            zipOut.closeEntry();
        } else {
// 文件夹的方式,获取文件夹下的子文件
            val fileList = file.list();
// 如果没有子文件, 则添加进去即可
            if (fileList.isEmpty()) {
                val zipEntry = ZipEntry(filePath + java.io.File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }
// 如果有子文件, 遍历子文件
            for (i in 0..fileList.size) {
                zipFiles(folderPath, filePath + java.io.File.separator + fileList[i], zipOut);
            }

        }

    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    fun copyFile(oldPath: String, newPath: String) {
        try {
            var bytesum = 0
            var byteread = 0
            val oldfile = File(oldPath)
            if (oldfile.exists()) { //文件存在时
                val inStream = FileInputStream(oldPath) //读入原文件
                val fs = FileOutputStream(newPath)
                var buffer = ByteArray(1444)
                var len = inStream.read(buffer)
                while (len != -1) {
                    fs.write(buffer, 0, len);
                    len = inStream.read(buffer)
                }
                inStream.close()
            }
        } catch (e: Exception) {
            println("复制单个文件操作出错")
            e.printStackTrace()

        }

    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    fun copyFolder(oldPath: String, newPath: String) {

        try {
            File(newPath).mkdirs() //如果文件夹不存在 则建立新文件夹
            val a = File(oldPath)
            val file = a.list()
            var temp: File? = null
            for (i in file.indices) {
                if (oldPath.endsWith(File.separator)) {
                    temp = File(oldPath + file[i])
                } else {
                    temp = File(oldPath + File.separator + file[i])
                }

                if (temp.isFile) {
                    val input = FileInputStream(temp)
                    val output = FileOutputStream(newPath + "/" +
                            temp.name.toString())

                    var buffer = ByteArray(1444)
                    var len = input.read(buffer)
                    while (len != -1) {
                        output.write(buffer, 0, len);
                        len = input.read(buffer)
                    }
                    output.flush()
                    output.close()
                    input.close()
                }
                if (temp.isDirectory) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i])
                }
            }
        } catch (e: Exception) {
            println("复制整个文件夹内容操作出错")
            e.printStackTrace()

        }

    }
}

