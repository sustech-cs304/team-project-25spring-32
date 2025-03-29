
package com.example.pa;

import android.app.Application;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化数据库
        DatabaseHelper.getInstance(this);
        Log.d("Database", "数据库已初始化1111111111");
    }
}