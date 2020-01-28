package com.miltolstoy.roundcalendar;

import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;

class Event {

    private String title;
    private long start;
    private long finish;

    Event(String title, long start, long finish) {
        Log.d(TAG, "Creating event. Title: " + title + ". Start: " + start + ". Finish: " + finish);
        this.title = title;
        this.start = start;
        this.finish = (finish != 0) ? finish : start;
    }

    Event(String title, String start, String finish) {
        this(title, parseLongSafe(start), parseLongSafe(finish));
    }

    String getTitle() {
        return title;
    }

    String getStartDate() {
        return formatToDate(start);
    }

    String getStartTime() {
        return formatToTime(start);
    }

    String getFinishTime() {
        return formatToTime(finish);
    }

    boolean isFullDay() {
        long timeDiff = this.finish - this.start;
        if (timeDiff == 0) {
            return false;
        }
        return (timeDiff % DateUtils.DAY_IN_MILLIS) == 0;
    }

    private String format(long milliSeconds, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private String formatToDate(long milliSeconds) {
        return format(milliSeconds, "dd.MM.yyyy");
    }

    private String formatToTime(long milliSeconds) {
        return format(milliSeconds, "HH:mm");
    }

    private static long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse event time. Value: " + value);
            return 0;
        }
    }

}
