package com.euBilmuhDuyuru.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.euBilmuhDuyuru.models.Annc;
import com.euBilmuhDuyuru.activities.MainActivity;
import com.euBilmuhDuyuru.PreferencesHelper;
import com.euBilmuhDuyuru.R;
import com.euBilmuhDuyuru.connection.RestClient;
import com.euBilmuhDuyuru.database.DatabaseHelper;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Yunus Emre Şeker on 29.05.2015.
 * -
 */
public class MyGcmListenerService extends GcmListenerService {

    public static final String TAG = "MyGcmListenerService";

    private static final int FAIL = 99;
    private static final int NEW = 0;
    private static final int UPDATE = 1;
    private static final int DELETE = 2;
    private static final int RESET = 3;
    private static final int TEST = 4;

    private SharedPreferences sharedPreferences;
    private DatabaseHelper db;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        sharedPreferences = PreferencesHelper.get(this);
        db = new DatabaseHelper(this);

        if (data == null) {
            return;
        }

        int type;
        try {
            type = Integer.parseInt(data.getString("type2"));
        } catch (NumberFormatException e) {

            // determine if its new or update type
            String title = data.getString("title");
            if (title != null) {
                if (title.isEmpty())
                    type = NEW;
                else
                    type = UPDATE;
            } else
                type = FAIL;
        }

        Log.d(TAG, "Message received, Type: " + type);
        switch (type) {
            case NEW:
                getNewAnncs();
                break;
            case UPDATE:
                parseUpdatedAnncs(data);
                break;
            case RESET:
                sharedPreferences.edit().putBoolean(PreferencesHelper.FIRST_RUN, true).apply();
                break;
            case TEST:
                sendNotification(data.getString("title"), data.getString("message"), TEST);
                break;
            case FAIL:
                sendNotification("Bilmuh Duyuru", "Bağlantı Hatası! Duyurular Alınamadı", FAIL);
                break;
            default:
                break;
        }

    }

    private void parseUpdatedAnncs(Bundle data) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        Type annctype = new TypeToken<List<Annc>>() {
        }.getType();
        try {
            List<Annc> updated = gson.fromJson(data.getString("message"), annctype);
            db.update(updated);
            sendNotification(updated.size() + " " + getString(R.string.annc_updated), updated, UPDATE);
        } catch (JsonParseException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void getNewAnncs() {
        int lastIndex = db.getLastIndex();
        if (lastIndex == 0) return;

        RestClient.get().getNewer(lastIndex,
                new Callback<ArrayList<Annc>>() {
                    @Override
                    public void success(ArrayList<Annc> anncs, Response response) {
                        if (anncs.size() > 0) {
                            db.write(anncs);
                            checkNotificationFilter(anncs);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i(TAG, "Error while connecting to server");
                        sendNotification("Bilmuh Duyuru", "Bağlantı Hatası! Duyurular Alınamadı", FAIL);
                    }
                });
    }

    private void checkNotificationFilter(ArrayList<Annc> anncs) {

        if (!sharedPreferences.getBoolean(PreferencesHelper.NOTIFICATION, true)) {
            return;
        }

        ArrayList<Annc> filtered = new ArrayList<>();
        String notifyFilter = sharedPreferences.getString(PreferencesHelper.NOTIFICATION_FILTER, "0");
        if (notifyFilter.equals("1")) {
            String name = sharedPreferences.getString(PreferencesHelper.USER_NAME, "");
            String no = sharedPreferences.getString(PreferencesHelper.USER_NUMBER, "0");
            if (name.equals("") || no.equals("0"))
                return;

            for (Annc annc : anncs) {
                if (annc.isContains(name, true) || annc.isContains(no, true))
                    filtered.add(annc);
            }

            if (filtered.size() > 0) {
                sendNotification(filtered.size() + " " + getString(R.string.yeni_duyuru), filtered, NEW);
                return;
            }
        }
        sendNotification(anncs.size() + " " + getString(R.string.yeni_duyuru), anncs, NEW);
    }

    private String generateTitle(List<Annc> list) {
        String result = "";
        for (Annc annc : list) {
            result += annc.getTitle() + "\n";
        }
        return result;
    }

    private void sendNotification(String title, String message, int id) {

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_ic_menu_paste_mtrl_am_alpha)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setTicker(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);

        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        mBuilder.setContentIntent(pendingIntent);

        mNotificationManager.notify(id, mBuilder.build());

    }

    private void sendNotification(String title, List<Annc> anncs, int id) {

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_ic_menu_paste_mtrl_am_alpha)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setTicker(title);

        if (anncs.size() > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(title);
            for (Annc annc : anncs) {
                inboxStyle.addLine(annc.getTitle());
            }
            mBuilder.setStyle(inboxStyle);
        } else {
            mBuilder.setContentText(anncs.get(0).getTitle());
        }

        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(id, mBuilder.build());

    }

    private void sendErrorNotification(String title, String message) {

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent retryIntent = new Intent(this, MyGcmListenerService.class);
        PendingIntent retryPendingIntent = PendingIntent.getService(this, 45, retryIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_ic_menu_paste_mtrl_am_alpha)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setTicker(message)
                        .addAction(R.drawable.ic_action_retry, getString(R.string.try_again), retryPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);

        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(133870, mBuilder.build());

    }
}
