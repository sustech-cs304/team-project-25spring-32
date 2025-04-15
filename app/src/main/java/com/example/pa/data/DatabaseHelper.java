package com.example.pa.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pa.data.Daos.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;


    // 单例模式
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建所有表
        db.execSQL(UserDao.CREATE_TABLE);
        Log.d("Database", "创建表User");

        db.execSQL(PhotoDao.CREATE_TABLE);
        Log.d("Database", "创建表Photo");

        db.execSQL(AlbumDao.CREATE_TABLE);
        Log.d("Database", "创建表Album");

        db.execSQL(AlbumPhotoDao.CREATE_TABLE);
        Log.d("Database", "创建表AlbumPhoto");

        db.execSQL(TagDao.CREATE_TABLE);
        Log.d("Database", "创建表Tag");

        db.execSQL(PhotoTagDao.CREATE_TABLE);
        Log.d("Database", "创建表PhotoTag");

        db.execSQL(SearchHistoryDao.CREATE_TABLE);
        Log.d("Database", "创建表SearchHistory");

        db.execSQL(MemoryVideoDao.CREATE_TABLE);
        Log.d("Database", "创建表MemoryVideo");

        db.execSQL(MemoryVideoPhotoDao.CREATE_TABLE);
        Log.d("Database", "创建表MemoryVideoPhoto");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除表的顺序要考虑外键约束
        db.execSQL("DROP TABLE IF EXISTS " + MemoryVideoPhotoDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MemoryVideoDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PhotoTagDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlbumPhotoDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TagDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlbumDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PhotoDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserDao.TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // 启用外键约束
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // 确保外键约束在每次打开数据库时都启用
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}