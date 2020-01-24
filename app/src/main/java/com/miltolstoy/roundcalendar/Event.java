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
    private boolean isFullDay;

    Event(String title, long start, long finish) {
        Log.d(TAG, "Creating event. Title: " + title + ". Start: " + start + ". Finish: " + finish);
        this.title = title;
        this.start = start;
        this.finish = finish;
        isFullDay = ((this.finish - this.start) == DateUtils.DAY_IN_MILLIS);
    }

    Event(String title, String start, String finish) {
        this(title, Long.parseLong(start), Long.parseLong(finish));
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
        return isFullDay;
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

}
