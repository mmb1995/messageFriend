package com.example.android.messagefriend.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageDateUtils {

    public static String convertLongToDate(long time) {
       Date date = new Date(time);
       String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
       return formattedDate;
    }
}
