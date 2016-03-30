package com.euBilmuhDuyuru.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.euBilmuhDuyuru.BuildConfig;
import com.euBilmuhDuyuru.PreferencesHelper;
import com.euBilmuhDuyuru.R;
import com.euBilmuhDuyuru.Utils;
import com.euBilmuhDuyuru.connection.RestClient;
import com.euBilmuhDuyuru.database.DatabaseHelper;
import com.euBilmuhDuyuru.gcm.RegistrationIntentService;
import com.euBilmuhDuyuru.models.Annc;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener {

    private SharedPreferences sharedPreferences;

    Preference register;
    Preference theme;
    CheckBoxPreference notification;
    boolean isLightTheme;
    final Activity self = this;
    private DatabaseHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferencesHelper.get(this);
        isLightTheme = sharedPreferences.getBoolean(PreferencesHelper.THEME, true);
        setTheme(isLightTheme ? R.style.PrefLight : R.style.PrefDark);
        addPreferencesFromResource(R.xml.setting);

        db = new DatabaseHelper(this);
        getListView().setDivider(null);
        getListView().setHeaderDividersEnabled(false);

        int bg = isLightTheme ? getResources().getColor(R.color.grey300) : getResources().getColor(R.color.grey900);
        getListView().setBackgroundColor(bg);

        register = findPreference(PreferencesHelper.NOTIFICATION_REGISTER);
        theme = findPreference(PreferencesHelper.THEME);
        notification = (CheckBoxPreference) findPreference(PreferencesHelper.NOTIFICATION);

        findPreference("reset_database").setOnPreferenceClickListener(this);
        findPreference("author").setOnPreferenceClickListener(this);
        findPreference("license").setOnPreferenceClickListener(this);
        register.setOnPreferenceClickListener(this);

        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                recreate();
                overridePendingTransition(0, 0);
                return true;
            }
        });

        if (sharedPreferences.getBoolean(PreferencesHelper.GCM_REGISTERED, false)) {
            register.setSummary(R.string.notifyReg_sum_s);
            if (notification != null)
                notification.setEnabled(true);
        } else {
            if (notification != null)
                notification.setEnabled(false);
            register.setSummary(R.string.notifyReg_sum_f);
        }

        setResult(RESULT_CANCELED);
    }

    @Override
    public boolean onPreferenceClick(Preference arg0) {
        if (arg0.getKey().equals("reset_database")) {
            final AlertDialog.Builder info = new AlertDialog.Builder(this);
            info.setMessage(getString(R.string.are_you_sure));
            info.setNegativeButton(R.string.no, null);
            info.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final AlertDialog w = makeWaitingDialog();
                    w.show();
                    RestClient.get().getAll(new Callback<ArrayList<Annc>>() {
                        @Override
                        public void success(ArrayList<Annc> anncs, Response response) {
                            db.deleteAll();
                            db.write(anncs);
                            w.dismiss();
                            setResult(RESULT_OK);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                }
            });
            info.show();
        }

        if (arg0.getKey().equals(PreferencesHelper.NOTIFICATION_REGISTER) &&
                !sharedPreferences.getBoolean(PreferencesHelper.GCM_REGISTERED, false)) {
            final AlertDialog.Builder info = new AlertDialog.Builder(this);
            info.setMessage(getString(R.string.notifyReg_dialog_mess_f));
            info.setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (Utils.checkPlayServices(self)) {
                        Intent register = new Intent(getBaseContext(), RegistrationIntentService.class);
                        startService(register);
                    }
                }
            });
            info.show();
        }

        if (arg0.getKey().equals("author")) {
            if (BuildConfig.DEBUG) {
                int newLast = db.deleteLast();
                Toast.makeText(getBaseContext(), "new last: " + newLast, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
            }
        }
        if (arg0.getKey().equals("license")) {
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new LicenseFragment())
                    .addToBackStack("license")
                    .commit();
        }
        return true;
    }

    private AlertDialog makeWaitingDialog() {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.db_recrating)
                .create();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public static class LicenseFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_res/raw/licenses.html");
            return webView;
        }
    }
}