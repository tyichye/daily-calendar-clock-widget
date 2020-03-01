package com.miltolstoy.roundcalendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static org.junit.Assert.assertEquals;


@RunWith(Enclosed.class)
public class CalendarAdapterTest {

    @Rule
    public GrantPermissionRule readRule = GrantPermissionRule.grant(Manifest.permission.READ_CALENDAR);
    @Rule
    public GrantPermissionRule writeRule = GrantPermissionRule.grant(Manifest.permission.WRITE_CALENDAR);

    private static int calendarId;
    private static Context context;

    private static final String calendarName = "dummyCalendar";
    private static final String accountName = "dummyName";
    private static final String accountType = "dummyType";
    private static final String defaultTitle = "dummyTitle";

    @RunWith(Parameterized.class)
    public static class GetEventsPositive {

        @Parameterized.Parameter()
        public long startTimeDelta;

        @Parameterized.Parameter(1)
        public long finishTimeDelta;

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

        @Parameterized.Parameters
        public static Collection parameters() {
            return Arrays.asList(new Object[][]{ // [day] {event}
                    {0, 0}, // [={ }=]
                    {HOUR_IN_MILLIS, -HOUR_IN_MILLIS}, // [ {} ]
                    {0, -HOUR_IN_MILLIS}, // [={ } ]
                    {HOUR_IN_MILLIS, 0}, // [ { }=]
                    {-HOUR_IN_MILLIS, -HOUR_IN_MILLIS}, // { [ } ]
                    {-HOUR_IN_MILLIS, 0}, // { [ }=]
                    {HOUR_IN_MILLIS, HOUR_IN_MILLIS}, // [ { ] }
                    {0, HOUR_IN_MILLIS}, // [={ ] }
                    {-HOUR_IN_MILLIS, HOUR_IN_MILLIS}, // { [ ] }
            });
        }

        @Test
        public void test() {
            long startTime = getDayStart() + startTimeDelta;
            long endTime = (getDayStart() + DAY_IN_MILLIS) + finishTimeDelta;
            boolean isAllDay = false;
            addEvent(defaultTitle, startTime, endTime, isAllDay);

            CalendarAdapter calendarAdapter = new CalendarAdapter(context, calendarId);
            calendarAdapter.requestCalendarPermissionsIfNeeded();
            List<Event> events = calendarAdapter.getTodayEvents();

            assertEquals(events.size(), 1);
            checkEvent(events.get(0), defaultTitle, startTime, endTime, isAllDay);
        }
    }

    public static class NonParametrized {

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
        public void noEventsInOneCalendar() {
            assertEquals(new CalendarAdapter(context, calendarId).getTodayEvents().size(), 0);
        }

        @Test
        public void noEventsInAllCalendars() {
            assertEquals(new CalendarAdapter(context).getTodayEvents().size(), 0);
        }

        @Test
        public void notExistingCalendar() {
            assertEquals(new CalendarAdapter(context, 9999).getTodayEvents().size(), 0);
        }

        @Test
        public void eventWithEndTime() {
            long startTime = getDayStart();
            long endTime = startTime + HOUR_IN_MILLIS;
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
            long startTime = getDayStart() + SECOND_IN_MILLIS;
            boolean isAllDay = false;
            addEvent(defaultTitle, startTime, "P1H", isAllDay);

            CalendarAdapter calendarAdapter = new CalendarAdapter(context, calendarId);
            calendarAdapter.requestCalendarPermissionsIfNeeded();
            List<Event> events = calendarAdapter.getTodayEvents();

            assertEquals(events.size(), 1);
            checkEvent(events.get(0), defaultTitle, startTime, (startTime + HOUR_IN_MILLIS), isAllDay);
        }

        @Test
        public void noCalendarId() {
            long startTime = getDayStart();
            long endTime = startTime + HOUR_IN_MILLIS;
            boolean isAllDay = false;
            addEvent(defaultTitle, startTime, endTime, isAllDay, calendarId);

            String anotherCalendarName = "anotherCalendar";
            int anotherCalendarId = addCalendar(anotherCalendarName, accountName, accountType);
            String anotherEventTitle = "anotherTitle";
            addEvent(anotherEventTitle, startTime, endTime, isAllDay, anotherCalendarId);

            CalendarAdapter calendarAdapter = new CalendarAdapter(context);
            calendarAdapter.requestCalendarPermissionsIfNeeded();
            List<Event> events = calendarAdapter.getTodayEvents();

            clearCalendar(anotherCalendarId);
            removeCalendar(anotherCalendarName, accountName, accountType);

            // events should be retrieved from both calendars
            assertEquals(events.size(), 2);
            checkEvent(events.get(0), defaultTitle, startTime, (startTime + HOUR_IN_MILLIS), isAllDay);
            checkEvent(events.get(1), anotherEventTitle, startTime, (startTime + HOUR_IN_MILLIS), isAllDay);
        }
    }

    private static int addCalendar(String calendarName, String accountName, String accountType) {
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

    private static void removeCalendar(String calendarName, String accountName, String accountType) {
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String where = NAME + "=? AND " + ACCOUNT_NAME + "=? AND " + ACCOUNT_TYPE + "=?";
        String[] selectionArgs = {calendarName, accountName, accountType};
        context.getContentResolver().delete(uri, where, selectionArgs);
    }

    private static void clearCalendar(int calendarId) {
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String where = CALENDAR_ID + " = ?";
        String[] selectionArgs = {String.valueOf(calendarId)};
        context.getContentResolver().delete(uri, where, selectionArgs);
    }

    private static void addEvent(String title, long startTime, long endTime, boolean isAllDay, int calendarId) {
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

    private static void addEvent(String title, long startTime, long endTime, boolean isAllDay) {
        addEvent(title, startTime, endTime, isAllDay, calendarId);
    }

    private static void addEvent(String title, long startTime, String duration, boolean isAllDay) {
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

    private static void checkEvent(Event event, String title, long startTime, long endTime, boolean isAllDay) {
        assertEquals(event.getTitle(), title);
        assertEquals(event.getStartTime(), millisToTime(startTime));
        assertEquals(event.getFinishTime(), millisToTime(endTime));
        assertEquals(event.isAllDay(), isAllDay);
    }

    private static String millisToTime(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private static long getDayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,
                0, 0);
        return calendar.getTime().getTime();
    }
}
