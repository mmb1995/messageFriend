package com.example.android.messagefriend.MessageUtils;

public class TextMessage {

    private String mPhoneNumber;
    private String mMessage;

    public TextMessage(String phoneNumber, String message) {
        this.mPhoneNumber = phoneNumber;
        this.mMessage = message;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getMessage() {
        return mMessage;
    }
}
