package com.miltolstoy.roundcalendar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventTest {

    private String defaultTitle = "defaultTitle";
    private String defaultStartStr = "123456789";
    private String defaultFinishStr = "987654321";
    private String defaultDurationStr = "P1H30M";
    private String defaultAllDayStr = "0";

    @Test
    public void getTitle() {
        Event event = new Event(defaultTitle, defaultStartStr, defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertEquals(event.getTitle(), defaultTitle);
    }

    @Test
    public void isAllDayTrue() {
        Event event = new Event(defaultTitle, defaultStartStr, defaultFinishStr, defaultDurationStr, "1");
        assertTrue(event.isAllDay());
    }

    @Test
    public void isAllDayFalse() {
        Event event = new Event(defaultTitle, defaultStartStr, defaultFinishStr, defaultDurationStr, "0");
        assertFalse(event.isAllDay());
    }

    @Test
    public void getStartTime() {
        Event event = new Event(defaultTitle, "123456789", defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertEquals(event.getStartTime(), "13:17");
    }

    @Test
    public void getStartTimeParseFailed() {
        Event event = new Event(defaultTitle, null, defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertEquals(event.getStartTime(), "03:00");
    }

    @Test
    public void getFinishTime() {
        Event event = new Event(defaultTitle, defaultStartStr, "987654321", defaultDurationStr, defaultAllDayStr);
        assertEquals(event.getFinishTime(), "13:20");
    }

    @Test
    public void getFinishTimeUsingDuration() {
        Event event = new Event(defaultTitle, "123456789", null, "P1H30M", defaultAllDayStr);
        assertEquals(event.getFinishTime(), "14:47");
    }

    @Test
    public void getFinishTimeDurationParseFailed() {
        Event event = new Event(defaultTitle, "123456789", null, null, defaultAllDayStr);
        assertEquals(event.getFinishTime(), "13:17");
    }

    @Test
    public void isStartedFirstDayHalfTrue() {
        Event event = new Event(defaultTitle, "12345678", defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertTrue(event.isStartedInFirstDayHalf());
    }

    @Test
    public void isStartedFirstDayHalfFalse() {
        Event event = new Event(defaultTitle, "123456789", defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertFalse(event.isStartedInFirstDayHalf());
    }
}
