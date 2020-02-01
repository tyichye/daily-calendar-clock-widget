package com.miltolstoy.roundcalendar;

import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import lombok.Getter;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;

class Event {

    @Getter private String title;
    private long start;
    private long finish;
    private long duration;
    private boolean allDay;
    private String recurRule;
    private String recurDate;

    Event(String title, String start, String finish, String duration, String allDay,
          String recurRule, String recurDate) {
        Log.d(TAG, "Creating event.\nTitle: " + title + "\nStart: " + start + "\nFinish: " + finish +
                "\nDuration: " + duration + "\nAll-day: " + allDay + "\nRecurrence rule: " + recurRule +
                "\nRecurrence date: " + recurDate);
        this.title = title;
        this.start = parseLongSafe(start);
        this.finish = parseLongSafe(finish);
        this.duration = Rfc5545Duration.toMilliSeconds(duration);
        this.finish = (this.finish != 0) ? this.finish : (this.start + this.duration);
        this.allDay = (allDay != null) && allDay.equals("1");
        this.recurRule = recurRule;
        this.recurDate = recurDate;
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

}
