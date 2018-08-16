package com.example.android.messagefriend;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    public static final String AUTOMATIC_RESPONSES_ALLOWED_KEY = "auto_response_switch_key";
    public static final String AUTOMATIC_RESPONSES_TEXT_KEY = "user_text_response_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
