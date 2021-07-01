package com.codepath.apps.restclienttemplate;
import android.text.format.DateUtils;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {
    public static final int COMPOSE_REQUEST_CODE = 1;
    public static final int REPLY_REQUEST_CODE = 2;
    public static final int TWEET_DETAIL_REQUEST_CODE = 3;
    public static final int USER_DETAIL_REQUEST_CODE = 4;
    public static final String TWEET_KEY_NAME = "tweet";
    public static final String POSITION_KEY_NAME = "position";
    public static final String TWEET_ADDED_KEY_NAME = "tweetAdded";
    public static final String USER_KEY_NAME = "user";
    public static final int RADIUS = 70;
    public static final int MARGIN = 15;
    public static final String twitterTimeFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    public static final String myTimeFormat = "hh:mmaa · MM/dd/yy";

    /* For time format conversion. Takes in a time string, its original format, and
     * its desired format. Returns the string in the desired format. */
    public static String convertTimeFormat(String time, String format1, String format2) {
        SimpleDateFormat sf = new SimpleDateFormat(format1, Locale.ENGLISH);
        sf.setLenient(true);
        String output = "";
        try {
            if (format2.equals("relative")) {
                // Get relative time ago
                long dateMillis = sf.parse(time).getTime();
                output = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } else {
                // Get string in desired format ex. "hh:mm MM/dd/yy"
                SimpleDateFormat sf2 = new SimpleDateFormat(format2, Locale.ENGLISH);
                sf2.setLenient(true);
                Date date = sf.parse(time);
                output = sf2.format(date);
            }
        } catch (ParseException e) {
            Log.e("TAG", "Error in time conversion", e);
        }
        return output;
    }
}
