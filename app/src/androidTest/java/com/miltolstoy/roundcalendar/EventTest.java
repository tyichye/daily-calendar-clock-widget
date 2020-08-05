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
    private int defaultColor = Color.BLACK;

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

    @Test
    public void getColorDefault() {
        Event event = new Event(defaultTitle, defaultStartStr, defaultFinishStr, defaultDurationStr, defaultAllDayStr);
        assertEquals(event.getColor(), Color.TRANSPARENT);
    }

    @Test
    public void getColor() {
        Event event = new Event(defaultTitle, defaultStartStr, defaultFinishStr, defaultDurationStr, defaultAllDayStr,
                defaultColor);
        assertEquals(event.getColor(), defaultColor);
    }
}
