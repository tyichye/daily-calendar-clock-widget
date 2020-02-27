package com.miltolstoy.roundcalendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.text.format.DateUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.DTEND;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.ALL_DAY;
import static android.provider.CalendarContract.Events.EVENT_TIMEZONE;
import static android.provider.CalendarContract.Events.TITLE;
import static android.provider.CalendarContract.Events.DURATION;
import static android.provider.CalendarContract.Calendars.NAME;
import static android.provider.CalendarContract.Calendars.ACCOUNT_NAME;
import static android.provider.CalendarContract.Calendars.ACCOUNT_TYPE;
import static android.provider.CalendarContract.CALLER_IS_SYNCADAPTER;
import static org.junit.Assert.assertEquals;


public class CalendarAdapterTest {

    @Rule
    public GrantPermissionRule readRule = GrantPermissionRule.grant(Manifest.permission.READ_CALENDAR);
    @Rule
    public GrantPermissionRule writeRule = GrantPermissionRule.grant(Manifest.permission.WRITE_CALENDAR);

    private int calendarId;
    private Context context;

    private final String calendarName = "dummyCalendar";
    private final String accountName = "dummyName";
    private final String accountType = "dummyType";
    private final String defaultTitle = "dummyTitle";


    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        calendarId = addCalendar(calendarName, accountName, accountType);
    }

    @After
    public void teardown() {
        clearCalendar(calendarId);
        removeCalendar(calendarName, accountName, accountType);
    }

    @Test
    public void noEvents() {
        assertEquals(new CalendarAdapter(context, calendarId).getTodayEvents().size(), 0);
    }

    @Test
    public void eventWithEndTime() {
        long startTime = getDayStart();
        long endTime = startTime + DateUtils.HOUR_IN_MILLIS;
        boolean isAllDay = false;
        addEvent(defaultTitle, startTime, endTime, isAllDay);

        CalendarAdapter calendarAdapter = new CalendarAdapter(context, calendarId);
        calendarAdapter.requestCalendarPermissionsIfNeeded();
        List<Event> events = calendarAdapter.getTodayEvents();

        assertEquals(events.size(), 1);
        checkEvent(events.get(0), defaultTitle, startTime, endTime, isAllDay);
    }

    @Test
    public void eventWithDuration() {
        long startTime = getDayStart() + DateUtils.SECOND_IN_MILLIS;
        boolean isAllDay = false;
        addEvent(defaultTitle, startTime, "P1H", isAllDay);

        CalendarAdapter calendarAdapter = new CalendarAdapter(context, calendarId);
        calendarAdapter.requestCalendarPermissionsIfNeeded();
        List<Event> events = calendarAdapter.getTodayEvents();

        assertEquals(events.size(), 1);
        checkEvent(events.get(0), defaultTitle, startTime, (startTime + DateUtils.HOUR_IN_MILLIS), isAllDay);
    }

    private int addCalendar(String calendarName, String accountName, String accountType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACCOUNT_NAME, accountName);
        contentValues.put(ACCOUNT_TYPE, accountType);
        contentValues.put(NAME, calendarName);

        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        uri = uri.buildUpon().appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(ACCOUNT_NAME, accountName)
                .appendQueryParameter(ACCOUNT_TYPE, accountType).build();
        Uri result = context.getContentResolver().insert(uri, contentValues);

        Pattern pattern = Pattern.compile("^content://com\\.android\\.calendar/calendars/(\\d+)\\?.*");
        Matcher matcher = pattern.matcher(result.toString());
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    private void removeCalendar(String calendarName, String accountName, String accountType) {
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String where = NAME + "=? AND " + ACCOUNT_NAME + "=? AND " + ACCOUNT_TYPE + "=?";
        String[] selectionArgs = {calendarName, accountName, accountType};
        context.getContentResolver().delete(uri, where, selectionArgs);
    }

    private void clearCalendar(int calendarId) {
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String where = CALENDAR_ID + " = ?";
        String[] selectionArgs = {String.valueOf(calendarId)};
        context.getContentResolver().delete(uri, where, selectionArgs);
    }

    private void addEvent(String title, long startTime, long endTime, boolean isAllDay) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, startTime);
        values.put(DTEND, endTime);
        values.put(ALL_DAY, isAllDay ? "1" : "0");
        values.put(TITLE, title);
        values.put(CALENDAR_ID, calendarId);
        values.put(EVENT_TIMEZONE, "Europe/Kiev");
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    private void addEvent(String title, long startTime, String duration, boolean isAllDay) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DTSTART, startTime);
        values.put(DURATION, duration);
        values.put(ALL_DAY, isAllDay ? "1" : "0");
        values.put(TITLE, title);
        values.put(CALENDAR_ID, calendarId);
        values.put(EVENT_TIMEZONE, "Europe/Kiev");
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    private void checkEvent(Event event, String title, long startTime, long endTime, boolean isAllDay) {
        assertEquals(event.getTitle(), title);
        assertEquals(event.getStartTime(), millisToTime(startTime));
        assertEquals(event.getFinishTime(), millisToTime(endTime));
        assertEquals(event.isAllDay(), isAllDay);
    }

    private String millisToTime(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private long getDayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,
                0, 0);
        return calendar.getTime().getTime();
    }
}
