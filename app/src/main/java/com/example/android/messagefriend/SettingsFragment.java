package com.example.android.messagefriend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment
                              implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String AUTOMATIC_RESPONSES_ALLOWED_KEY = "auto_response_switch_key";
    public static final String AUTOMATIC_RESPONSES_TEXT_KEY = "user_text_response_key";

    private static final String TAG = "SettingsFragment";
    /**
     * Required empty public constructor
     */
    public SettingsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from the XML resource
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG,"Preference Changed!");
        Log.i(TAG, key);
        if (key.equals(AUTOMATIC_RESPONSES_ALLOWED_KEY)) {
            Preference switchPref = findPreference(key);
            boolean enabled = sharedPreferences.getBoolean(key,false);
            if (enabled) {
                switchPref.setSummary("Enabled");
            } else {
                switchPref.setSummary("Disabled");
            }
        } else if(key.equals(AUTOMATIC_RESPONSES_TEXT_KEY)) {
            Log.i(TAG,"Updating Automated message");
            Preference messagePref = findPreference(key);
            Log.i(TAG, sharedPreferences.getString(key,"nothing"));
            messagePref.setSummary(sharedPreferences.getString(key,""));
            Log.i(TAG, sharedPreferences.getString(key,"failed"));
        }

    }
}
