package com.miltolstoy.roundcalendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    void requestCalendarPermissionsIfNeeded() {
        Log.d(TAG, "Checking READ_CALENDAR permission");
        int permission_status = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR);
        if (permission_status == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "READ_CALENDAR permission granted");
            return;
        }

        Log.d(TAG, "Requesting READ_CALENDAR permission");
        int request_status = 0;
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                request_status);
        Log.d(TAG, "Permission request status: " + request_status);
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
                new String[] {TITLE, DTSTART, DTEND, DURATION, ALL_DAY}, where, selectionArgs, DTSTART);

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
                    cursor.getString(4));
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
}
