package com.robi027.gorun.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by robi on 4/30/2019.
 */

public class PrefUtil {
    public static final String My_PREF = "My_PREF";

    public static SharedPreferences sharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void putString(Context context, String key, String value){
        sharedPreferences(context).edit().putString(key, value).apply();
    }

    public static void putBoolean(Context context, String key, boolean value){
        sharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key){
        return sharedPreferences(context).getBoolean(key, false);
    }

    public static String getString(Context context, String key){
        return sharedPreferences(context).getString(key, null);
    }
}
