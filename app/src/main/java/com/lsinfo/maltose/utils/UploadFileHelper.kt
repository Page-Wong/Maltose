package com.lsinfo.maltose.utils

import com.lsinfo.maltose.Config
import com.lsinfo.maltose.bean.ResultBean
import com.lsinfo.maltose.bean.ResultCode
import okhttp3.*
import okio.Buffer
import okio.BufferedSink
import okio.Okio
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by G on 2018-04-25.
 */
object UploadFileHelper {
    //--------ContentType
    private val MEDIA_OBJECT_STREAM = MediaType.parse("multipart/form-data")
    //--------上传延时时间
    private val WRITE_TIME_OUT:Long  = 60
    //--------反馈延时时间
    private val READ_TIME_OUT:Long  = 60
    private val mOkHttpClient by lazy { OkHttpClient() }

    var result = ResultBean()
    //------------------------
    //带参数同步上传文件
    fun syncParamsUploadFile(url: String, file: File, params:MutableMap<String,Any?> = mutableMapOf()):String?{
        params["file"] = file
        val response = createParamsOkHttpCall(url, params).execute()
        return response.body()!!.string()
    }
    //带参数异步上传文件
    fun asyncParamsUploadFile(url: String, file: File, params:MutableMap<String,Any?> = mutableMapOf(), uploadCallBackListener: UploadCallBackListener){
        params["file"] = file
        createParamsOkHttpCall(url, params).enqueue(object :Callback{
            override fun onFailure(c: Call, e: IOException) {
                uploadCallBackListener.onError(e)
            }
            override fun onResponse(c: Call, response: Response) {
                val json = response.body()!!.string()
                try {
                    val obj = JSONObject(json)
                    if (obj.optInt("code") == 1) {
                        try{
                            uploadCallBackListener.onSuccess(obj)
                        }
                        catch (e:Exception){
                            LoggerHandler.crashLog.e(e)
                            result.code = ResultCode.OPERATE_ERROR
                            result.exception = e
                        }
                    } else {
                        LoggerHandler.commLog.w(mapOf<String, String>(
                                Pair("msg", "UploadFileHelper asyncParamsUploadFile"),
                                Pair("json", json)
                        ))
                        uploadCallBackListener.onFail(obj)
                    }
                }
                catch (e: Exception){
                    LoggerHandler.crashLog.e(e)
                    result.code = ResultCode.HTTP_ERROR
                    result.exception = e
                }
                finally {
                    LoggerHandler.commLog.i(mapOf<String, String>(
                            Pair("msg", "UploadFileHelper asyncParamsUploadFile"),
                            Pair("result", result.toString())
                    ))
                    uploadCallBackListener.onCallBack(result)
                }
                response.body()!!.close()
            }
        })

    }
    //------创建一个没有带参数的Call
    fun createNoParamsOkHttpCall(url: String, b: ByteArray):Call{
        val requestUrl = url
        val requestBody = RequestBody.create(MEDIA_OBJECT_STREAM,b)
        val request = Request.Builder().url(requestUrl).post(requestBody).build()
        return mOkHttpClient.newBuilder().readTimeout(READ_TIME_OUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIME_OUT,TimeUnit.SECONDS).build().newCall(request)
    }
    //------创建一个带参数的Call
    fun createParamsOkHttpCall(url: String, baseParams:MutableMap<String,Any?>): Call {
        //-----AppConstant.HOST 上传图片的Server的BASE_URL http://xxx.com
        val requestUrl = url
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        val params = processParams(baseParams)
        params.forEach( action = {
            if(it.value is File){
                builder.addFormDataPart(it.key, (it.value as File).name,
                        RequestBody.create(null, (it.value as File)))
            }else{
                builder.addFormDataPart(it.key,it.value.toString())
            }
        })
        val body = builder.build()
        val request = Request.Builder().url(requestUrl).post(body).build()
        return mOkHttpClient.newBuilder().readTimeout(READ_TIME_OUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS).build().newCall(request)

    }

    //解析Server返回的数据获取图片路径，
    /*
       {"code":200,"msg":"上传成功","data":{"path":""}}
   */
    fun getResponseToPath(response:String):String{
        val dataJsonObj = JSONObject(response).get("data") as JSONObject
        return dataJsonObj.get("path") as String
    }


    fun processParams(params: MutableMap<String, Any?>): MutableMap<String, Any?> {
        if (params["file"] == null || params["file"] !is File) return params
        var dataMap = sortedMapOf<String, Any?>()
        dataMap["token"] = Config.TOKEN
        dataMap["timestamp"] = Date().time.toString()
        dataMap["fileMd5"] = SecurityUtils.md5EncryptWithSlat(params["file"] as File)
        dataMap["sign"] =SecurityUtils.sign(dataMap)
        dataMap["file"] = params["file"]
        return dataMap
    }

    //回调方法
    interface UploadCallBackListener{
        fun onSuccess(json: JSONObject){}

        fun onFail(json: JSONObject){}

        fun onError(e: Exception){
            result.code = ResultCode.OPERATE_ERROR
            result.exception = e
            onCallBack(result)
        }

        fun onCallBack(result: ResultBean){}
    }
}