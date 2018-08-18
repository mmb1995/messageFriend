package com.example.android.messagefriend.MessageUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    // interface
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String sender = smsMessage.getDisplayOriginatingAddress();
            String messageBody = smsMessage.getMessageBody();
            long timeInMillis = smsMessage.getTimestampMillis();

            // Pass the message body to the interface
            mListener.messageReceived(sender, messageBody, timeInMillis);
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

}
