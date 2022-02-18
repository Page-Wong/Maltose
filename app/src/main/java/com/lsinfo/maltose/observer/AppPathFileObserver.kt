package com.lsinfo.maltose.observer

import android.os.FileObserver
import android.content.Context
import android.util.Log
import com.lsinfo.maltose.Config
import com.lsinfo.maltose.db.PlayInfoDbManager
import com.lsinfo.maltose.utils.LoggerHandler
import com.lsinfo.maltose.utils.SecurityUtils
import java.io.File
import java.util.*


/**
 * Created by G on 2018-04-13.
 */
class AppPathFileObserver(val context: Context, val path: String?, val mask: Int = FileObserver.ALL_EVENTS) : FileObserver(path) {

    private var mObservers: ArrayList<SingleFileObserver> = arrayListOf()

    /** Only modification events  */
    val CHANGES_ONLY = (FileObserver.CREATE or FileObserver.MODIFY or FileObserver.DELETE or FileObserver.CLOSE_WRITE
            or FileObserver.DELETE_SELF or FileObserver.MOVE_SELF or FileObserver.MOVED_FROM or FileObserver.MOVED_TO)

    override fun startWatching() {
        mObservers.clear()

        val stack = Stack<String>()
        stack.push(path)

        while (!stack.isEmpty()) {
            val parent = stack.pop()
            mObservers.add(SingleFileObserver(parent, mask))
            val path = File(parent)
            val files = path.listFiles() ?: continue
            for (f in files) {
                if (f.isDirectory && f.name != "."
                        && f.getName() != "..") {
                    stack.push(f.path)
                }
            }
        }

        for (i in 0 until mObservers.size) {
            val sfo = mObservers[i]
            sfo.startWatching()
        }
    };

    override fun stopWatching() {
        for (i in 0 until mObservers.size) {
            val sfo = mObservers[i]
            sfo.stopWatching()
        }
        mObservers.clear()
    };

    override fun onEvent(event: Int, path: String?) {
        val action = event and CHANGES_ONLY
        if (action != 0){
            LoggerHandler.fileLog.i(mapOf<String,String>(
                    Pair("msg","文件或目录有改动"),
                    Pair("event",action.toString()),
                    Pair("path",path?:"")
            ))
            if (path != null)
                SecurityUtils.checkPlayInfo(context, getPlayInfoIdByPath(path))
        }
    }

    /**
     * 根据文件地址获取节目id
     */
    private fun getPlayInfoIdByPath(path: String): String{
        if (!path.startsWith(Config.APP_DATA_PATH)) return "none"
        val f = File(path)
        return if (f.parentFile.absolutePath == Config.APP_DATA_PATH)
            f.absolutePath.replace(Config.APP_DATA_PATH, "").substring(1)
        else
            getPlayInfoIdByPath(f.parentFile.absolutePath)
    }

    inner class SingleFileObserver(private val mPath: String?, mMask: Int = FileObserver.ALL_EVENTS): FileObserver(mPath, mMask) {

        override fun onEvent(event: Int, path: String?) {
            val newPath = "$mPath/$path"
            this@AppPathFileObserver.onEvent(event, newPath)
        }
    }
}