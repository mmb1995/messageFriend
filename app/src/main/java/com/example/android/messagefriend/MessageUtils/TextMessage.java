package com.example.android.messagefriend.MessageUtils;

public class TextMessage {

    private String mPhoneNumber;
    private String mMessage;
    private String date;

    public TextMessage(String phoneNumber, String message, String date) {
        this.mPhoneNumber = phoneNumber;
        this.mMessage = message;
        this.date = date;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getDate() {return date;}
}
