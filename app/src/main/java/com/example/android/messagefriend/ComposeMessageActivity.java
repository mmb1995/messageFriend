package com.example.android.messagefriend;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ComposeMessageActivity extends AppCompatActivity {

    private static final String TAG="ComposeMessageActivity";

    // Request code for searching for contacts
    private static final int PICK_CONTACT_REQUEST = 1;

    private static final String EMULATOR_PHONE_NUMBER = "5554";

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    EditText mPhoneNumberEditText;
    ImageButton mSearchContactsButton;


    ImageButton mSendMessageButton;
    EditText mComposeMessageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        // Gets Reference to views related to the contact for the SMS message
        mPhoneNumberEditText = (EditText) findViewById(R.id.phone_number_edit_text);
        mSearchContactsButton = (ImageButton) findViewById(R.id.contact_search_button);

        // Sets onClickListener for mSearchContactsButton
        mSearchContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickContact();
            }
        });


        // get Reference to EditText for composing messages
        mComposeMessageEditText =  (EditText) findViewById(R.id.compose_message_edit_text);

        // Get Reference to send message button
        mSendMessageButton = (ImageButton) findViewById(R.id.send_message_button);

        // set onClickListener for send message button
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // Set up ActionBar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
    }


    /**
     * Creates and sends an Intent for searching for contacts to send messages to
     */
    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));

        // Only show user contacts that have a phone number
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);

        // launch intent
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    /**
     * Receives the information about the contact selected by the user
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request to respond to
        if (requestCode == PICK_CONTACT_REQUEST) {
            //Checks if the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();

                // Gets the phone number for the selected contact
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Performs the query on the contact to get the NUMBER column
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);

                // displays the number for the selected contact
                displayPhoneNumber(number);

            }
        }
    }

    /**
     * Displays the selected phone number
     * @param number the phone number for the selected contact
     */
    private void displayPhoneNumber(String number) {
        mPhoneNumberEditText.setText("");
        mPhoneNumberEditText.append(number);
    }

    /**
     * Sends an SMS text message
     */
    private void sendMessage() {
        // Message to be sent
        String mCurrentTextMessage = mComposeMessageEditText.getText().toString();

        // Gets the current phone number and formats it
        String phoneNumber = PhoneNumberUtils.formatNumber(mPhoneNumberEditText.getText().toString());

        Log.i(TAG,"Checking Permissions for SMS message");

        // Attempts to send SMS message
        try {
            // Message successfully sent
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, mCurrentTextMessage,
                    null, null);
            Toast.makeText(this,"SMS Sent Successfully", Toast.LENGTH_LONG).show();
            mComposeMessageEditText.getText().clear();

        } catch (Exception e) {
            // Message failed to send
            Toast.makeText(this, "SMS Failed to Send, Please try again", Toast.LENGTH_LONG).show();

        }

    }

    /**
     * Creates the options menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose_activity_options_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.search_button:
                // launch the settings activity
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
