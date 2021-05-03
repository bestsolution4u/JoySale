package com.hitasoft.app.helper;

import android.content.Context;
import android.util.Log;

import com.hitasoft.app.joysale.R;
import com.hitasoft.app.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {

    private static final String TAG = DateHelper.class.getSimpleName();
    private final Context context;

    public DateHelper(Context context) {
        this.context = context;
    }

    public String getUTCFromTimeStamp(long timeStamp) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, new Locale("en"));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        myDate.setTime(timeStamp);
//        Log.i(TAG, "getUTCFromTimeStamp: " + utcFormat.format(myDate));
        return utcFormat.format(myDate);
    }

    public String convertToDate(String date) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, new Locale("en"));
        DateFormat newFormat = new SimpleDateFormat(Constants.UI_DATE_PATTERN, new Locale("en"));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = newFormat.format(myDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "convertToDate: " + newDate);
        return newDate;
    }

    public String timeStampToDate(long timeStamp) {
        DateFormat newFormat = new SimpleDateFormat(Constants.UI_DATE_PATTERN, new Locale("en"));
        Date myDate = new Date();
        String newDate = "";
        myDate.setTime(timeStamp);
        newDate = newFormat.format(myDate);
        Log.i(TAG, "timeStampToDate: " + newDate);
        return newDate;
    }

    public String getPostedDate(String postedDate) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, new Locale("en"));
        DateFormat newFormat = new SimpleDateFormat(Constants.UI_DATE_PATTERN, new Locale("en"));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(postedDate);
            Calendar smsTime = Calendar.getInstance();
            smsTime.setTimeZone(TimeZone.getDefault());
            smsTime.setTime(myDate);

            Calendar now = Calendar.getInstance();
            final String dateTimeFormatString = "dd MMM yyyy";
            if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
                return context.getString(R.string.today);
            } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
                return context.getString(R.string.yesterday);
            } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
                return android.text.format.DateFormat.format(dateTimeFormatString, smsTime).toString();
            } else {
                return android.text.format.DateFormat.format("MMMM dd yyyy", smsTime).toString();
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getDateFromUTC: " + newDate);
        return newDate;
    }

    public String getGraphWeeks(String date) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, Locale.ENGLISH);
        DateFormat monthFormat = new SimpleDateFormat("MMM", LocaleManager.getLocale(context.getResources()));
        DateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = monthFormat.format(myDate) + " " + dateFormat.format(myDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    public String getGraphMonths(String date) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, Locale.ENGLISH);
        DateFormat newFormat = new SimpleDateFormat("MMM", LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = newFormat.format(myDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    public String getGraphYears(String date) {
        DateFormat utcFormat = new SimpleDateFormat(Constants.UTC_DATE_PATTERN, new Locale("en"));
        DateFormat newFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = newFormat.format(myDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }
}
