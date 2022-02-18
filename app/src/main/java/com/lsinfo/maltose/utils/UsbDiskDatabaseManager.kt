package com.lsinfo.maltose.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.example.yf_rk3288_api.YF_RK3288_API_Manager
import com.lsinfo.maltose.App
import java.io.*


/**
 * Created by G on 2019-08-19.
 */
class UsbDiskDatabaseManager private constructor(context: Context) {


    // A mapping from assets database file to SQLiteDatabase object
    private val databases = HashMap<String, SQLiteDatabase>()

    // Context of application
    private var context: Context? = null

    private val databaseFilepath: String
        get() = String.format(databasepath, context!!.applicationInfo.packageName)

    init {
        this.context = context
    }

    /**
     * Get a assets database, if this database is opened this method is only return a copy of the opened database
     * @param dbfile, the assets file which will be opened for a database
     * @return, if success it return a SQLiteDatabase object else return null
     */
    fun getDatabase(dbfile: String): SQLiteDatabase? {
        if (databases[dbfile] != null) {
            Log.i(tag, String.format("Return a database copy of %s", dbfile))
            return databases[dbfile] as SQLiteDatabase
        }
        if (context == null)
            return null

        Log.i(tag, String.format("Create database %s", dbfile))
        val spath = databaseFilepath
        val sfile = getDatabaseFile(dbfile)


        var path = File(spath)
        if (!path.exists() && !path.mkdir()) {
            Log.i(tag, "Create \"$spath\" fail!")
            return null
        }

        var file = File(sfile)
//        val dbs = context!!.getSharedPreferences(UsbDiskDatabaseManager::class.java.toString(), 0)
//        val flag = dbs.getBoolean(dbfile, false) // Get Database file flag, if true means this database file was copied and valid

        if (file.exists())
            file.delete()
        if (!file.exists() && !file.createNewFile()) {
            Log.i(tag, "Create \"$sfile\" fail!")
            return null
        }

        if (!copyUsbDiskToFilesystem(dbfile, sfile)) {
            Log.i(tag, String.format("Copy %s to %s fail!", dbfile, sfile))
            return null
        }

        val db = SQLiteDatabase.openDatabase(sfile, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS)
        if (db != null) {
            databases[dbfile] = db
        }
        return db
    }

    private fun getDatabaseFile(dbfile: String): String {
        return "$databaseFilepath/$dbfile"
    }

    private fun copyUsbDiskToFilesystem(src: String, des: String): Boolean {
        Log.i(tag, "Copy $src to $des")
        var istream: InputStream? = null
        var ostream: OutputStream? = null
        try {
            val yfapi = YF_RK3288_API_Manager(App.getInstance())
            val UDISKpath = yfapi.yfgetUSBPath()
            istream = FileInputStream("$UDISKpath/$src")
            ostream = FileOutputStream(des)

            var buffer = ByteArray(1024)
            var len = istream.read(buffer)
            while (len != -1) {
                ostream.write(buffer, 0, len);
                len = istream.read(buffer)
            }
            istream!!.close()
            ostream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (istream != null)
                    istream.close()
                if (ostream != null)
                    ostream.close()
            } catch (ee: Exception) {
                ee.printStackTrace()
            }

            return false
        }

        return true
    }

    /**
     * Close assets database
     * @param dbfile, the assets file which will be closed soon
     * @return, the status of this operating
     */
    fun closeDatabase(dbfile: String): Boolean {
        if (databases[dbfile] != null) {
            val db = databases[dbfile] as SQLiteDatabase
            db.close()
            databases.remove(dbfile)
            return true
        }
        return false
    }

    companion object {
        private val tag = "QMWJ" // for LogCat
        private val databasepath = "/data/data/%s/database" // %s is packageName

        // Singleton Pattern
        /**
         * Get a UsbDiskDatabaseManager object
         * @return, if success return a UsbDiskDatabaseManager object, else return null
         */
        var manager: UsbDiskDatabaseManager? = null
            private set

        /**
         * Initialize UsbDiskDatabaseManager
         * @param context, context of application
         */
        fun initManager(context: Context) {
            if (manager == null) {
                manager = UsbDiskDatabaseManager(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.N)
                /**
         * Close all assets database
         */
        fun closeAllDatabase() {
            Log.i(tag, "closeAllDatabase")
            if (manager != null) {
                manager!!.databases.forEach { s, sqLiteDatabase ->
                    if (sqLiteDatabase != null)
                        sqLiteDatabase.close()
                }
                manager!!.databases.clear()
            }
        }
    }
}