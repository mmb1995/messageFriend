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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public class ReadMessageActivity extends AppCompatActivity
             implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    private static final String TAG = "ReadMessageActivity";

    private ListView mMessagesListView;

    private MessageAdapter messageAdapter;

    private ArrayList<TextMessage> smsMessagesList = new ArrayList<TextMessage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        // Get reference to the ListView for storing messages
        mMessagesListView = (ListView) findViewById(R.id.messages_list_view);

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

        // Prepare the loader. Either re-connect with an existing one
        // or start a new one.
        getSupportLoaderManager().initLoader(0,null, this);
        //checkForPermissions();
    }

    public void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            //refreshInbox();
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

        if (messageAdapter != null) {
            messageAdapter.add(newMessage);
            messageAdapter.notifyDataSetChanged();
        }

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

    private ArrayList<TextMessage> mMessagesList;

    /**
     * Returns a new CursorLoader for Querying the devices inbox for SMS messages
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String SMS_INBOX = "content://sms/inbox";
        Uri CONTACT_URI = Uri.parse(SMS_INBOX);
        CursorLoader cursorLoader = new CursorLoader(this, CONTACT_URI, null, null,
                null, null);
        return cursorLoader;
    }

    /**
     * Retrieves information from the cursor and sets up the adapter for the list view displaying
     * SMS messages
     * @param loader a Loader<Cursor> object
     * @param data the cursor containing the SMS messages in the devices inbox
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMessagesList = getMessagesFromInbox(data);
        messageAdapter = new MessageAdapter(this, mMessagesList);
        mMessagesListView.setAdapter(messageAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Returns a list containing the phone number and message for all the SMS messages currently
     * in the devices inbox
     */
    private ArrayList<TextMessage> getMessagesFromInbox(Cursor mInboxCursor) {
        ArrayList<TextMessage> inboxMessageList = new ArrayList<TextMessage>();

        // get the index for the phone number and message
        int indexBody = mInboxCursor.getColumnIndex("body");
        int indexAddress = mInboxCursor.getColumnIndex("address");

        // There are no messages to retrieve
        if (indexBody < 0 || !mInboxCursor.moveToFirst()) {
            return inboxMessageList;
        }

        //messageAdapter.clear();

        do {
            // Create new TextMessage object to store contents from the message
            TextMessage currentMessage = new TextMessage(mInboxCursor.getString(indexAddress),
                    mInboxCursor.getString(indexBody));
            inboxMessageList.add(currentMessage);
        } while (mInboxCursor.moveToNext());

        return inboxMessageList;
    }


    /**
     * Custom ArrayAdapter for displaying the contents of SMS messages
     */
    public class MessageAdapter extends ArrayAdapter<TextMessage> {
        private Cursor mCursor;
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

            // Gets the current TextMessage
            TextMessage currentMessage = mMessagesList.get(position);

            // Displays the phone number
            TextView numberTextView = (TextView) listItem.findViewById(R.id.phone_number_text_view);
            numberTextView.setText(currentMessage.getPhoneNumber());

            // Displays the message content
            TextView messageTextView = (TextView) listItem.findViewById(R.id.sms_message_text_view);
            messageTextView.setText(currentMessage.getMessage());

            return listItem;
        }

        public void setMessageList(ArrayList<TextMessage> messageList) {
            mMessagesList = messageList;
            notifyDataSetChanged();
        }
    }
}
