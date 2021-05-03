package com.hitasoft.app.external;

import android.content.Context;
import android.content.res.Resources;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.joysale.R;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hitasoft on 9/1/17.
 */

public class TimeAgo {

    private static String TAG = TimeAgo.class.getSimpleName();
    protected Context context;
    Locale locale;

    public TimeAgo(Context context) {
        this.context = context;
        locale = LocaleManager.getLocale(context.getResources());
    }

    public String timeAgo(long millis) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));

        long diff = new Date().getTime() - millis;
        Resources r = context.getResources();
        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double weeks = days / 7;
        double years = days / 365;
        String words;
        if (seconds < 60) {
            words = r.getString(R.string.time_ago_seconds, nf.format(Math.round(seconds)));
        } else if (seconds < 120) {
            words = r.getString(R.string.time_ago_minute);
        } else if (minutes < 60) {
            words = r.getString(R.string.time_ago_minutes, nf.format(Math.round(minutes)));
        } else if (minutes < 120) {
            words = r.getString(R.string.time_ago_hour);
        } else if (hours < 24) {
            String temp = nf.format(Math.round(hours));
            words = r.getString(R.string.time_ago_hours, temp);
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day);
        } else if (days < 30) {
            words = r.getString(R.string.time_ago_days, nf.format(Math.round(days)));
        } else if (days < 60) {
            words = r.getString(R.string.time_ago_month);
        } else if (days < 365) {
            words = r.getString(R.string.time_ago_months, nf.format(Math.round(days / 30)));
        } else if (years < 2) {
            words = r.getString(R.string.time_ago_year);
        } else {
            words = r.getString(R.string.time_ago_years, nf.format(years));
        }
        return words.trim();
    }
}
