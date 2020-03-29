package com.miltolstoy.roundcalendar;

import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import lombok.Getter;

import static com.miltolstoy.roundcalendar.Logging.TAG;

class Event {

    @Getter private String title;
    private long start;
    private long finish;
    @Getter private boolean allDay;
    @Getter private int color = Color.TRANSPARENT;

    Event(String title, String start, String finish, String duration, String allDay, int color) {
        this(title, start, finish, duration, allDay);
        this.color = color;
    }

    Event(String title, String start, String finish, String duration, String allDay) {
        this(title, parseLongSafe(start), parseLongSafe(finish), parseDurationSafe(duration),
                ((allDay != null) && allDay.equals("1")));
    }

    Event(String title, long start, long finish, long duration, boolean allDay) {
        Log.d(TAG, "Creating event.\nTitle: " + title + "\nStart: " + start + "\nFinish: " + finish +
                "\nDuration: " + duration + "\nAll-day: " + allDay + "\n");

        this.title = title;
        this.start = start;
        this.finish = (finish != 0) ? finish : (this.start + duration);
        this.allDay = allDay;
    }

    String getStartTime() {
        return formatToTime(start);
    }

    String getFinishTime() {
        return formatToTime(finish);
    }

    boolean isStartedInFirstDayHalf() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        return calendar.get(Calendar.HOUR_OF_DAY) < 12;
    }


    private String formatToTime(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private static long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse event time. Value: " + value);
            return 0;
        }
    }

    private static long parseDurationSafe(String duration) {
        try {
            return Rfc5545Duration.toMilliSeconds(duration);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
            return 0;
        }
    }

}
