package com.example.android.messagefriend;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.messagefriend.MessageUtils.NotificationUtils;
import com.example.android.messagefriend.MessageUtils.SmsListener;
import com.example.android.messagefriend.MessageUtils.SmsReceiver;
import com.example.android.messagefriend.MessageUtils.TextMessage;

import java.util.ArrayList;
import java.util.List;

public class ReadMessageActivity extends AppCompatActivity {
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    private static final String TAG = "ReadMessageActivity";

    private ListView mMessagesListView;

    private ArrayAdapter messageAdapter;

    private ArrayList<TextMessage> smsMessagesList = new ArrayList<TextMessage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        // Get reference to the ListView for storing messages
        mMessagesListView = (ListView) findViewById(R.id.messages_list_view);

        // Creates the Adapter
        messageAdapter = new MessageAdapter(this, smsMessagesList);

        // Sets an adapter for the ListView
        mMessagesListView.setAdapter(messageAdapter);


        // Set up Broadcast Receiver and callback method
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String phoneNumber, String messageText) {
                onSmsMessageReceived(phoneNumber, messageText);
            }
        });

        // Set up Action Bar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        // Set up Floating Action button
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);

        final Context context = getApplicationContext();
        // Set up onClickListener for Floating Action Button
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Button Pressed", Toast.LENGTH_SHORT).show();
                launchComposeMessageActivity();
            }
        });

        checkForPermissions();
    }

    public void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            refreshInbox();
        }
    }

    /**
     * Updates the inbox with the current SMS messages on the device
     */
    public void refreshInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor mInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"),null,
                null,null, null );
        int indexBody = mInboxCursor.getColumnIndex("body");
        int indexAddress = mInboxCursor.getColumnIndex("address");

        // There are no messages to retrieve
        if (indexBody < 0 || !mInboxCursor.moveToFirst()) {
            return;
        }

        messageAdapter.clear();

        do {
            // Create new TextMessage object to store contents from the message
            TextMessage currentMessage = new TextMessage(mInboxCursor.getString(indexAddress),
                    mInboxCursor.getString(indexBody));
            messageAdapter.add(currentMessage);
        } while (mInboxCursor.moveToNext());
    }


    /**
     * Prompts the user to allow the app to grant permission to read sms messages
     */
    public void getPermissionToReadSMS() {

    }

    /**
     * Updates the ListView to include the received message
     * @param phoneNumber phone number for the phone that sent the message
     * @param message the content of the received sms message
     */
    public void onSmsMessageReceived(String phoneNumber, String message) {
        Log.i(TAG, message);
        TextMessage newMessage = new TextMessage(phoneNumber, message);
        messageAdapter.add(newMessage);
        messageAdapter.notifyDataSetChanged();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoReply = sharedPref.getBoolean(SettingsFragment.AUTOMATIC_RESPONSES_ALLOWED_KEY, false);
        if (autoReply) {
            // Attempts to send auto SMS message
            try {
                // Message successfully sent
                String autoMessage = sharedPref.getString(SettingsFragment.AUTOMATIC_RESPONSES_TEXT_KEY,"Hello");
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, autoMessage,
                        null, null);
                Toast.makeText(this,"SMS Sent Successfully", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                // Message failed to send
                Toast.makeText(this, "SMS Failed to Send, Please try again", Toast.LENGTH_LONG).show();

            }
        }

        // Send Notification to user that a new Message has been received
        sendNewMessageNotification(phoneNumber, message);


    }

    private void sendNewMessageNotification(String phoneNumber, String message) {
        NotificationUtils.notifyUserOfNewMessage(this, phoneNumber, message);
    }

    /**
     * This method creates and launches an intent to start the composeMessageActivity
     */
    private void launchComposeMessageActivity() {
        // Creates Intent to start the composeMessageActivity
        Intent composeMessageIntent = new Intent(this, ComposeMessageActivity.class);

        // Launches the composeMessageActivity
        startActivity(composeMessageIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.read_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                launchSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Launches the settings activity when the user clicks on the settings action in the option
     * menu.
     */
    private void launchSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Custom ArrayAdapter for displaying the contents of SMS messages
     */
    public class MessageAdapter extends ArrayAdapter<TextMessage> {

        private Context mContext;
        private List<TextMessage> mMessagesList = new ArrayList<TextMessage>();

        public MessageAdapter(@NonNull Context context,  ArrayList<TextMessage> messageList) {
            super(context, 0, messageList);
            mContext = context;
            mMessagesList = messageList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.message_list_item_content,
                        parent,false);
            }
            TextMessage currentMessage = mMessagesList.get(position);
            TextView numberTextView = (TextView) listItem.findViewById(R.id.phone_number_text_view);
            numberTextView.setText(currentMessage.getPhoneNumber());

            TextView messageTextView = (TextView) listItem.findViewById(R.id.sms_message_text_view);
            messageTextView.setText(currentMessage.getMessage());

            return listItem;
        }
    }
}
