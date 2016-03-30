package com.euBilmuhDuyuru;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Yunus Emre Şeker on 01.06.2015.
 * -
 */
public class Utils {

    public static Locale trLoc = new Locale("tr", "TR");
    public static TimeZone tzTurkey = TimeZone.getTimeZone("Europe/Istanbul");
    public static DateFormat dateFormat = new SimpleDateFormat("dd MMMM yy, EEEE", Utils.trLoc);
    public static DateFormat dateFullFormat = new SimpleDateFormat("dd MMMM yy, EEEE HH:mm", Utils.trLoc);
    public static DateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Utils.trLoc);

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static {
        dateFormat.setTimeZone(tzTurkey);
        dateFullFormat.setTimeZone(tzTurkey);
        dbFormat.setTimeZone(tzTurkey);
    }

    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getBaseContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity, resultCode,
                        Utils.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Snackbar.make(activity.findViewById(android.R.id.content), R.string.common_google_play_services_unsupported_text,
                        Snackbar.LENGTH_LONG).show();
                Log.e("checkPlayServices", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public static void makeThemedSnackBar(Activity activity, boolean lightTheme, int message, int length, int actionMessage, @Nullable View.OnClickListener actionListener) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, length);
        if (actionMessage != 0)
            snackbar.setAction(actionMessage, actionListener);
        View sv = snackbar.getView();
        sv.setBackgroundColor(activity.getResources().getColor(lightTheme ? R.color.grey900 : R.color.grey300));
        TextView tv = (TextView) sv.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(lightTheme ? Color.WHITE : Color.BLACK);
        snackbar.show();
    }

}
