package com.opensource.roundcalendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static android.provider.CalendarContract.Calendars.ACCOUNT_NAME;
import static android.provider.CalendarContract.Calendars._ID;
import static android.provider.CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
import static android.provider.CalendarContract.Events.TITLE;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.DTEND;
import static android.provider.CalendarContract.Events.DURATION;
import static android.provider.CalendarContract.Events.ALL_DAY;
import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.DISPLAY_COLOR;
import static com.opensource.roundcalendar.Logging.TAG;

class CalendarAdapter {

    private static CalendarAdapter instance = null;

    private Context context;
    private List<String> calendarIds;
    private int daysShift;


    public static CalendarAdapter getInstance() {
        if (instance == null) instance = new CalendarAdapter();
        return instance;
    }

    public void setCalendarIds(List<String> calendarIds) {
        this.calendarIds = calendarIds;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setDaysShift(int daysShift) {
        this.daysShift = daysShift;
    }

    static List<CalendarInfo> getCalendars(Context context) {
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        final String[] projection = new String[] {_ID, ACCOUNT_NAME, CALENDAR_DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null /*selection*/, null /*selectionArgs*/,
                null /*sortOrder*/);
        if (cursor == null || cursor.getCount() == 0) {
            Log.e(TAG, "No results");
            return null;
        }

        List<CalendarInfo> calendarInfoList = new ArrayList<>();
        cursor.moveToFirst();
        Log.d(TAG, "Calendars list:");
        do {
            CalendarInfo info = new CalendarInfo(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                    cursor.getString(2));
            Log.d(TAG, info.toDebugString());
            calendarInfoList.add(info);
        } while (cursor.moveToNext());

        cursor.close();
        return calendarInfoList;
    }

    List<Event> getTodayEvents() {
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
        long dayStart = getDayStart();
        ContentUris.appendId(builder, dayStart);
        ContentUris.appendId(builder, dayStart + DateUtils.DAY_IN_MILLIS);

        String where = null;
        String[] selectionArgs = null;
        if (calendarIds != null && !calendarIds.isEmpty()) {
            StringBuilder whereBuilder = new StringBuilder("(");
            for (int i = 0; i < calendarIds.size(); i++) {
                whereBuilder.append(CALENDAR_ID + "=? OR ");
            }
            whereBuilder.setLength(whereBuilder.length() - 4);
            whereBuilder.append(")");
            where = whereBuilder.toString();
            selectionArgs = calendarIds.toArray(new String[0]);
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

    public int getDayOfMonth() {
        return getDayStartCalendar().get(Calendar.DATE);
    }
}
