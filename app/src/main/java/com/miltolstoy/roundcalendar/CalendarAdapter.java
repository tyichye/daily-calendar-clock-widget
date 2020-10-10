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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.provider.CalendarContract.Events.TITLE;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.DTEND;
import static android.provider.CalendarContract.Events.DURATION;
import static android.provider.CalendarContract.Events.ALL_DAY;
import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.DISPLAY_COLOR;
import static com.miltolstoy.roundcalendar.Logging.TAG;

class CalendarAdapter {

    private Context context;
    private int calendarId;
    private int daysShift;

    static final int CALENDAR_EMPTY_ID = -1;

    CalendarAdapter(Context context) {
        this(context, CALENDAR_EMPTY_ID, 0);
    }

    CalendarAdapter(Context context, int calendarId) {
        this(context, calendarId, 0);
    }

    CalendarAdapter(Context context, int calendarId, int daysShift) {
        this.context = context;
        this.calendarId = calendarId;
        this.daysShift = daysShift;
    }

    List<Event> getTodayEvents() {
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
        long dayStart = getDayStart();
        ContentUris.appendId(builder, dayStart);
        ContentUris.appendId(builder, dayStart + DateUtils.DAY_IN_MILLIS);

        String where = null;
        String[] selectionArgs = null;
        if (calendarId != CALENDAR_EMPTY_ID) {
            where = CALENDAR_ID + "=?";
            selectionArgs = new String[] {String.valueOf(calendarId)};
        }
        Cursor cursor = context.getContentResolver().query(builder.build(),
                new String[] {TITLE, DTSTART, DTEND, DURATION, ALL_DAY, DISPLAY_COLOR}, where, selectionArgs, DTSTART);

        List<Event> events = new ArrayList<>();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
        } else {
            Log.w(TAG, "No events for today");
            return events;
        }

        Log.d(TAG, "Today events:");
        do {
            Event event = new Event(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), Integer.parseInt(cursor.getString(5)));
            int todayDayNumber = getDayOfMonth();
            if ( event.isAllDay() && (event.getFinishDate() == todayDayNumber) ) {
                continue; // all-day event, which actually ended yesterday, but have finish time today at 3:00
            }
            events.add(event);
        } while (cursor.moveToNext());
        Log.d(TAG, "Total: " + events.size());

        cursor.close();
        return events;
    }

    Calendar getDayStartCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        calendar.add(Calendar.DAY_OF_MONTH, daysShift);
        return calendar;
    }

    boolean isCalendarShifted() {
        return (daysShift != 0);
    }

    private long getDayStart() {
        return getDayStartCalendar().getTime().getTime();
    }

    private int getDayOfMonth() {
        return getDayStartCalendar().get(Calendar.DATE);
    }
}
