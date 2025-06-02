package com.example.pa.util;

import android.content.Context;
import android.content.SharedPreferences;

public class removeLogin {
    public static void removeLoginStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("is_logged_in").apply();
        prefs.edit().remove("auth_token").apply();
    }
}
