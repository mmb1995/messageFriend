package com.example.android.messagefriend;

import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.android.messagefriend.MessageUtils.MessageDateUtils;
import com.example.android.messagefriend.MessageUtils.NotificationUtils;
import com.example.android.messagefriend.MessageUtils.SmsListener;
import com.example.android.messagefriend.MessageUtils.SmsReceiver;
import com.example.android.messagefriend.MessageUtils.TextMessage;

import java.util.ArrayList;
import java.util.List;

public class ReadMessageActivity extends AppCompatActivity
             implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int SMS_PERMISSIONS_REQUEST = 1;

    private static final String TAG = "ReadMessageActivity";

    private ListView mMessagesListView;

    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        // Get reference to the ListView for storing messages
        mMessagesListView = (ListView) findViewById(R.id.messages_list_view);

        // Set up Broadcast Receiver and callback method
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String phoneNumber, String messageText, long time) {
                onSmsMessageReceived(phoneNumber, messageText, time);
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
                launchComposeMessageActivity();
            }
        });

        // Check to see if the user has granted the proper permissions
        checkSmsPermissions();
    }


    /**
     * Prompts the user to allow the app to grant permission to read and send sms messages
     */
    public void checkSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // prompt user for necessary permissions
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow the app permission to read and send SMS Messages",
                        Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS}, SMS_PERMISSIONS_REQUEST);
        } else {
            // Permission already granted set up loader to get SMS messages in the phones inbox
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case SMS_PERMISSIONS_REQUEST: {
                // If request cancelled the results arrays will be empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    getSupportLoaderManager().initLoader(0, null, this);

                } else {
                    // permission denied
                }

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Updates the ListView to include the received message
     * @param phoneNumber phone number for the phone that sent the message
     * @param message the content of the received sms message
     */
    public void onSmsMessageReceived(String phoneNumber, String message, long time) {
        Log.i(TAG, message);
        String date = MessageDateUtils.convertLongToDate(time);
        TextMessage newMessage = new TextMessage(phoneNumber, message, date);

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

        // get the index for the phone number, message, and date
        int indexBody = mInboxCursor.getColumnIndex("body");
        int indexAddress = mInboxCursor.getColumnIndex("address");
        int dateIndex = mInboxCursor.getColumnIndex("date");

        // There are no messages to retrieve
        if (indexBody < 0 || !mInboxCursor.moveToFirst()) {
            return inboxMessageList;
        }

        do {
            // Create new TextMessage object to store contents from the message
            String date = MessageDateUtils.convertLongToDate(mInboxCursor.getLong(dateIndex));
            String phoneNumber = PhoneNumberUtils.formatNumber(mInboxCursor.getString(indexAddress));
            TextMessage currentMessage = new TextMessage(phoneNumber,
                    mInboxCursor.getString(indexBody), date);
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
        private ColorGenerator generator;


        public MessageAdapter(@NonNull Context context,  ArrayList<TextMessage> messageList) {
            super(context, 0, messageList);
            mContext = context;
            mMessagesList = messageList;
            generator = ColorGenerator.MATERIAL;
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
            TextView numberTextView = (TextView) listItem.findViewById(R.id.phone_number_display_view);
            numberTextView.setText(currentMessage.getPhoneNumber());

            // Displays the message content
            TextView messageTextView = (TextView) listItem.findViewById(R.id.message_display_view);
            messageTextView.setText(currentMessage.getMessage());

            // Displays the date
            TextView dateTextView = (TextView) listItem.findViewById(R.id.time_display_view);
            dateTextView.setText(currentMessage.getDate());

            // Display the icon next to the message
            ImageView iconTextView = (ImageView) listItem.findViewById(R.id.contact_icon_image_view);
            String iconLetter = "";
            if (Character.isDigit(currentMessage.getPhoneNumber().charAt(0))) {
                iconLetter = "#";
            } else {
                iconLetter = "" + currentMessage.getPhoneNumber().charAt(0);
            }

            // Create a new TextDrawable for the icon's background
            TextDrawable drawable = TextDrawable.builder().buildRound(iconLetter,
                    generator.getRandomColor());
            iconTextView.setImageDrawable(drawable);
            return listItem;
        }

        public void setMessageList(ArrayList<TextMessage> messageList) {
            mMessagesList = messageList;
            notifyDataSetChanged();
        }
    }
}
