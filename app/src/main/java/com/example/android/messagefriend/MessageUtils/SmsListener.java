package com.example.android.messagefriend.MessageUtils;


public interface SmsListener {
    public void messageReceived(String phoneNumber, String messageText);
}
