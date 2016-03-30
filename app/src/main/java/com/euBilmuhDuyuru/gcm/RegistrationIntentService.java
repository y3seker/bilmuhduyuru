package com.euBilmuhDuyuru.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.euBilmuhDuyuru.PreferencesHelper;
import com.euBilmuhDuyuru.R;
import com.euBilmuhDuyuru.connection.RegisterResponse;
import com.euBilmuhDuyuru.connection.RestClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Yunus Emre Şeker on 29.05.2015.
 * -
 */
public class RegistrationIntentService extends IntentService {

    private final static String TAG = "RegistrationService";

    private SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferencesHelper.get(this);

        try {
            synchronized (TAG) {

                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.d("TOKEN", token);
                sendRegistrationToServer(token);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(PreferencesHelper.GCM_REGISTERED, false).apply();
        }
    }

    private void sendRegistrationToServer(final String token) {

        RestClient.get().register(token, new Callback<RegisterResponse>() {
            @Override
            public void success(RegisterResponse registerResponse, Response response) {
                if (registerResponse.regCode == RegisterResponse.SUCCESS)
                    sharedPreferences.edit().putBoolean(PreferencesHelper.GCM_REGISTERED, true).apply();
            }

            @Override
            public void failure(RetrofitError error) {
                sharedPreferences.edit().putBoolean(PreferencesHelper.GCM_REGISTERED, false).apply();
            }
        });

    }

}
