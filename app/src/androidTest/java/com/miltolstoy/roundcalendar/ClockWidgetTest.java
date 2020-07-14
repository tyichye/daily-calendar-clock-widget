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
import android.graphics.Point;
import android.graphics.RectF;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class ClockWidgetTest {

    private Point defaultScreenSize = new Point(1000, 2000);

    @Test
    public void xSideChosen() {
        ClockWidget widget = new ClockWidget(new Point(1000, 2000));
        Point center = widget.getCenter();
        assertEquals(center.x, 500);
        assertEquals(center.y, 555);
    }

    @Test
    public void ySideChosen() {
        ClockWidget widget = new ClockWidget(new Point(2000, 1000));
        Point center = widget.getCenter();
        assertEquals(center.x, 1000);
        assertEquals(center.y, 500);
    }

    @Test
    public void getBorderColor() {
        assertEquals(new ClockWidget(defaultScreenSize).getBorderColor(), Color.WHITE);
    }

    @Test
    public void getFillColor() {
        assertEquals(new ClockWidget(defaultScreenSize).getFillColor(), Color.TRANSPARENT);
    }

    @Test
    public void getDigitColor() {
        assertEquals(new ClockWidget(defaultScreenSize).getDigitColor(), Color.WHITE);
    }

    @Test
    public void getEventTitleColor() {
        assertEquals(new ClockWidget(defaultScreenSize).getEventTitleColor(), Color.WHITE);
    }

    @Test
    public void getBorderWidth() {
        assertEquals(new ClockWidget(defaultScreenSize).getBorderWidth(), 10);
    }

    @Test
    public void getHandWidth() {
        assertEquals(new ClockWidget(defaultScreenSize).getHandWidth(), 5);
    }

    @Test
    public void getDotRadius() {
        assertEquals(new ClockWidget(defaultScreenSize).getDotRadius(), 10);
    }

    @Test
    public void getSmallDigitsSize() {
        assertEquals(new ClockWidget(defaultScreenSize).getSmallDigitSize(), 25);
    }

    @Test
    public void getBigDigitsSize() {
        assertEquals(new ClockWidget(defaultScreenSize).getBigDigitSize(), 40);
    }

    @Test
    public void getDateSize() {
        assertEquals(new ClockWidget(defaultScreenSize).getDateSize(), 55);
    }

    @Test
    public void getTitleSize() {
        assertEquals(new ClockWidget(defaultScreenSize).getTitleSize(), 37);
    }

    @Test
    public void getCenter() {
        assertEquals(new ClockWidget(defaultScreenSize).getCenter(), new Point(500, 555));
    }

    @Test
    public void getRadius() {
        assertEquals(new ClockWidget(defaultScreenSize).getRadius(), (float) 424, 0);
    }

    @Test
    public void getHourMarkersCoordinates() {
        List<List<Point>> expCoordinates = new ArrayList<>();
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(500, 131), new Point(500, 158))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(800, 255), new Point(781, 274))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(924, 555), new Point(897, 555))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(800, 855), new Point(781, 836))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(500, 979), new Point(500, 952))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(200, 855), new Point(219, 836))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(76, 555), new Point(103, 555))));
        expCoordinates.add(new ArrayList<>(Arrays.asList(new Point(200, 255), new Point(219, 274))));
        assertThat(new ClockWidget(defaultScreenSize).getHourMarkersCoordinates(), is(expCoordinates));
    }

    @Test
    public void getHourDotsCoordinates() {
        List<Point> expCoordinates = new ArrayList<>(Arrays.asList(new Point(610, 145), new Point(712, 188),
                new Point(867, 343), new Point(910, 445), new Point(910, 665), new Point(867, 767), new Point(712, 922),
                new Point(610, 965), new Point(390, 965), new Point(288, 922), new Point(133, 767), new Point(90, 665),
                new Point(90, 445), new Point(133, 343), new Point(288, 188), new Point(390, 145)));
        assertThat(new ClockWidget(defaultScreenSize).getHourDotsCoordinates(), is(expCoordinates));
    }

    @Test
    public void getDigitsCoordinates() {
        List<Point> expCoordinates = new ArrayList<>(Arrays.asList(new Point(500, 103), new Point(625, 120),
                new Point(734, 166), new Point(827, 239), new Point(899, 334), new Point(943, 444), new Point(958, 563),
                new Point(941, 682), new Point(894, 792), new Point(820, 887), new Point(731, 955),
                new Point(620, 1002), new Point(500, 1019), new Point(380, 1002), new Point(269, 955),
                new Point(180, 887), new Point(106, 792), new Point(59, 682), new Point(42, 563), new Point(57, 444),
                new Point(101, 334), new Point(173, 239), new Point(266, 166), new Point(375, 120)));
        assertThat(new ClockWidget(defaultScreenSize).getDigitsCoordinates(), is(expCoordinates));
    }

    @Test
    public void getDateCoordinates() {
        assertEquals(new ClockWidget(defaultScreenSize).getDateCoordinates(), new Point(27, 66));
    }

    @Test
    public void getDayOfWeekCoordinates() {
        assertEquals(new ClockWidget(defaultScreenSize).getDayOfWeekCoordinates(), new Point(27, 125));
    }

    @Test
    public void getWidgetCircleObject() {
        assertEquals(new ClockWidget(defaultScreenSize).getWidgetCircleObject(), new RectF(76, 131, 924, 979));
    }

    @Test
    public void getEventDegrees() {
        Event event = new Event("title", "123456789", "987654321", "42424242", "0");
        ClockWidget.EventDegreeData eventData = new ClockWidget(defaultScreenSize).getEventDegrees(event);
        assertEquals(eventData.getStart(), 109.25, 0);
        assertEquals(eventData.getSweep(), 0.75, 0);
    }

    @Test
    public void calculateEventTitlePointNonZeroLength() {
        assertEquals(new ClockWidget(defaultScreenSize).calculateEventTitlePoint(100, 42), new Point(850, 617));
    }

    @Test
    public void calculateEventTitlePointZeroLength() {
        assertEquals(new ClockWidget(defaultScreenSize).calculateEventTitlePoint(100, 0), new Point(891, 624));
    }

    @Test
    public void getCurrentTimeHandCoordinates() {
        List<Point> coordinates = new ClockWidget(defaultScreenSize).getCurrentTimeHandCoordinates();
        assertEquals(coordinates.size(), 2);
        assertEquals(coordinates.get(0), new Point(500, 555));
        // second point cannot be checked without Calendar mocking
        assertNotEquals(coordinates.get(1), new Point(500, 555));
    }
}
