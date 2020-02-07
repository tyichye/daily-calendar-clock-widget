package com.miltolstoy.roundcalendar;

import android.graphics.Color;
import android.graphics.Point;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
