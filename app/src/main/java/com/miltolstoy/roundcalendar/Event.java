/*
Round Calendar
Copyright (C) 2020 Mil Tolstoy <miltolstoy@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.miltolstoy.roundcalendar;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
        this.color = ColorUtils.blendARGB(color, Color.BLACK, 0.1f);
    }

    Event(String title, String start, String finish, String duration, String allDay) {
        this(title, parseLongSafe(start), parseLongSafe(finish), parseDurationSafe(duration),
                ((allDay != null) && allDay.equals("1")));
    }

    Event(String title, long start, long finish, long duration, boolean allDay) {
        Log.d(TAG, "Creating event." +
                "\nTitle: " + title +
                "\nStart: " + formatToDateTime(start) + " (" + start + ")" +
                "\nFinish: " + formatToDateTime(finish) + " (" + finish + ")" +
                "\nDuration: " + duration +
                "\nAll-day: " + allDay + "\n");

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

    // tal's code
    boolean isEnd(long currentTime)
    {
//        System.out.println(title);
//        System.out.println(currentTime);
//        System.out.println(finish);
//        System.out.println(currentTime > finish);

        return formatToTime(currentTime).compareTo(getFinishTime()) > 0 && getFinishDate() == getStartDate();
    }
    // end tal's code



    // relevant to know how to draw the arc of the event - up-down or down-up
    boolean isFinishedInFirstDayHalf() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finish);
        return calendar.get(Calendar.HOUR_OF_DAY) < 12;
    }

    int getStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        return calendar.get(Calendar.DATE);
    }

    int getFinishDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finish);
        return calendar.get(Calendar.DATE);
    }


    private String formatToTime(long milliSeconds) {
        return formatTimestamp(milliSeconds, "HH:mm");
    }

    private String formatToDateTime(long milliSeconds) {
        return formatTimestamp(milliSeconds, "dd.MM.YY HH:mm");
    }

    private String formatTimestamp(long milliSeconds, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
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
