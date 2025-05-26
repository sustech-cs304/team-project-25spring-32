package com.example.pa.util;

import android.content.Context;
import android.content.SharedPreferences;

public class checkLogin {
    public static boolean checkLoginStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }
}
