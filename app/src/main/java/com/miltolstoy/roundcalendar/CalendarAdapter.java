package com.miltolstoy.roundcalendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
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

class CalendarAdapter {

    private Activity context;
    private final String tag = "CalendarAdapter";

    CalendarAdapter(Activity context) {
        this.context = context;
    }

    void requestCalendarPermissionsIfNeeded()
    {
        int permission_status = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR);
        if (permission_status == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(tag, "READ_CALENDAR permission granted");
            return;
        }
        int request_status = 0;
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_CALENDAR},
                request_status);
        Log.d(tag, "request_status: " + request_status);
    }

    List<Event> getTodayEvents() {
        List<Event> events = new ArrayList<>();
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
        long dayStart = getCurrentDayStart();
        ContentUris.appendId(builder, dayStart);
        ContentUris.appendId(builder, dayStart + DateUtils.DAY_IN_MILLIS);
        Cursor cursor = context.getContentResolver()
                .query(
                        builder.build(),
                        new String[] { "title", "dtstart", "dtend"}, null,
                        null, null);
        cursor.moveToFirst();
        Log.d(tag, "Today events:");
        do {
            Event event = new Event(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            Log.d(tag, String.format("%s: %s %s-%s", event.getTitle(), event.getStartDate(), event.getStartTime(), event.getFinishTime()));
            events.add(event);
        } while (cursor.moveToNext());
        Log.d(tag, "Total: " + events.size());
        return events;
    }

    private long getCurrentDayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        return calendar.getTime().getTime();
    }

}
