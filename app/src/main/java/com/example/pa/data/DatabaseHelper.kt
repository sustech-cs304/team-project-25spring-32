package com.example.pa.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.pa.data.Daos.AlbumDao
import com.example.pa.data.Daos.AlbumPhotoDao
import com.example.pa.data.Daos.GroupDao
import com.example.pa.data.Daos.GroupMemberDao
import com.example.pa.data.Daos.MemoryVideoDao
import com.example.pa.data.Daos.MemoryVideoPhotoDao
import com.example.pa.data.Daos.PhotoDao
import com.example.pa.data.Daos.PhotoTagDao
import com.example.pa.data.Daos.PostDao
import com.example.pa.data.Daos.SearchHistoryDao
import com.example.pa.data.Daos.TagDao
import com.example.pa.data.Daos.UserDao

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            // 创建所有表
            db.execSQL(UserDao.CREATE_TABLE)
            Log.d("Database", "创建表User")

            db.execSQL(PhotoDao.CREATE_TABLE)
            Log.d("Database", "创建表Photo")

            db.execSQL(AlbumDao.CREATE_TABLE)
            Log.d("Database", "创建表Album")

            db.execSQL(AlbumPhotoDao.CREATE_TABLE)
            Log.d("Database", "创建表AlbumPhoto")

            db.execSQL(TagDao.CREATE_TABLE)
            Log.d("Database", "创建表Tag")

            db.execSQL(PhotoTagDao.CREATE_TABLE)
            Log.d("Database", "创建表PhotoTag")

            db.execSQL(SearchHistoryDao.CREATE_TABLE)
            Log.d("Database", "创建表SearchHistory")

            db.execSQL(MemoryVideoDao.CREATE_TABLE)
            Log.d("Database", "创建表MemoryVideo")

            db.execSQL(MemoryVideoPhotoDao.CREATE_TABLE)
            Log.d("Database", "创建表MemoryVideoPhoto")

            db.execSQL(GroupDao.CREATE_TABLE)
            Log.d("Database", "创建表Groups")

            db.execSQL(GroupMemberDao.CREATE_TABLE)
            Log.d("Database", "创建表GroupMembers")

            db.execSQL(PostDao.CREATE_TABLE)
            Log.d("Database", "创建表Posts")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "创建表失败: " + e.message)
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // 删除表的顺序要考虑外键约束
            db.execSQL("DROP TABLE IF EXISTS " + MemoryVideoPhotoDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + MemoryVideoDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + PhotoTagDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + AlbumPhotoDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + TagDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + AlbumDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + PhotoDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + PostDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + GroupMemberDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + GroupDao.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + UserDao.TABLE_NAME)

            // 创建新表
            onCreate(db)
            Log.d("DatabaseHelper", "数据库升级成功")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "数据库升级失败: " + e.message)
            throw e
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // 启用外键约束
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // 确保外键约束在每次打开数据库时都启用
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    companion object {
        /**
         * AI-generated-content
         * tool: Deepseek
         * version: latest
         * usage: I directly copy the code from its response and modify the logic of some method, add some
         * methods we need but it did not generate, and add some logs.
         */
        private const val DATABASE_NAME = "pa.db"
        private const val DATABASE_VERSION = 2


        // 单例模式
        private var instance: DatabaseHelper? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }
}