package com.example.android.messagefriend.MessageUtils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.messagefriend.R;
import com.example.android.messagefriend.ReadMessageActivity;

public class NotificationUtils {

    /**
     * This id can be used to access the notification after it is displayed.
     * The value is arbitrary and is just used to provide a reference to the notification
     */
    private static final int MESSAGE_REMINDER_NOTIFICATION_ID = 112;


    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int MESSAGE_REMINDER_PENDING_INTENT_ID = 114;

    /**
     * This notification channel id is used to link notifications to this channel.
     * This is required for apps running on Oreo
     */
    private static final String MESSAGE_REMINDER_NOTIFICATION_CHANNEL_ID = "message_notification_channel";



    public static void notifyUserOfNewMessage(Context context, String phoneNumber, String message) {
        // Creates the NotificationManager
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Creates a notification channel for Android O devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    MESSAGE_REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Build the Notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context,MESSAGE_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.baseline_message_black)
                .setContentTitle(phoneNumber)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        // Set the notifications priority to PRIORITY_HIGH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(MESSAGE_REMINDER_PENDING_INTENT_ID, notificationBuilder.build());



    }
    /**
     * This method returns a PendingIntent that will trigger when the notification is pushed
     * This intent should open up the ReadMessagesActivity which serves as the apps main activity
     */
    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, ReadMessageActivity.class);

        return PendingIntent.getActivity(
                context,
                MESSAGE_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();

        Bitmap largeIcon = BitmapFactory.decodeResource(res, android.support.v4.R.drawable.notification_icon_background);
        return largeIcon;
    }
}
