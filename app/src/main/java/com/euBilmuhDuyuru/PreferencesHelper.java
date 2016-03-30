package com.euBilmuhDuyuru;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Yunus Emre Şeker on 05.03.2015.
 * -
 */
public class PreferencesHelper {

    public static final String PREFS_GCM = "com.google.android.gms.appid";

    public static final String FIRST_RUN = "first_run";
    public static final String THEME = "theme_light";
    public static final String USER_NAME = "user_name";
    public static final String USER_NUMBER = "user_number";
    public static final String NOTIFICATION = "notification_enabled";
    public static final String NOTIFICATION_FILTER = "notification_filter";
    public static final String NOTIFICATION_CATEGORY = "notification_category";
    public static final String NOTIFICATION_REGISTER = "notification_register";
    public static final String ANNC_COUNT = "annc_count";
    public static final String LAST_INDEX = "last_index";

    public static final String GCM_REGISTERED = "gcm_registered_to_server";

    public static final String SEARCH_IN_CONTENT = "search_in_content";


    private PreferencesHelper(Context context) {
    }

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getGcmPrefs(Context context) {
        return context.getSharedPreferences(PREFS_GCM, Context.MODE_PRIVATE);
    }

    public static boolean isGcmRegistered(Context context){
        return get(context).getBoolean(GCM_REGISTERED,false);
    }

}
