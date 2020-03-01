package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.miltolstoy.roundcalendar.Logging.TAG;

class ClockWidget {

    @Getter private final int borderColor = Color.WHITE;
    @Getter private final int fillColor = Color.TRANSPARENT;
    @Getter private final int digitColor = Color.WHITE;
    @Getter private final int eventTitleColor = Color.WHITE;
    private final int[] degrees = {0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180, 195, 210, 225, 240, 255,
            270, 285, 300, 315, 330, 345};

    @Getter private int borderWidth;
    @Getter private int handWidth;
    @Getter private int dotRadius;
    @Getter private int smallDigitSize;
    @Getter private int bigDigitSize;
    @Getter private int dateSize;
    @Getter private int titleSize;
    private int markersLength;
    private double tiltedMarkersLength;
    private double digitRadiusPadding;
    private int dateXPadding;
    private int dateYPadding;

    @Getter private Point center;
    @Getter private float radius;
    private Point screenSize;
    private List<Point> hoursCoordinates;

    ClockWidget(Point screenSize) {
        this.screenSize = screenSize;
        int minSide = (screenSize.x < screenSize.y) ? screenSize.x : screenSize.y;
        calculateSizesAccordingToScreen(minSide);
    }

    List<List<Point>> getHourMarkersCoordinates() {
        List<List<Point>> markers = new ArrayList<>();
        for (int i = 0; i < hoursCoordinates.size(); i++) {
            if (i % 3 != 0) {
                continue;
            }

            Point hTStart = hoursCoordinates.get(i);
            double xStop, yStop;
            switch (i) {
                case 0:
                    xStop = hTStart.x;
                    yStop = hTStart.y + markersLength;
                    break;
                case 6:
                    xStop = hTStart.x - markersLength;
                    yStop = hTStart.y;
                    break;
                case 12:
                    xStop = hTStart.x;
                    yStop = hTStart.y - markersLength;
                    break;
                case 18:
                    xStop = hTStart.x + markersLength;
                    yStop = hTStart.y;
                    break;
                default:
                    xStop = i < 12 ? hTStart.x - tiltedMarkersLength : hTStart.x + tiltedMarkersLength;
                    yStop = (i < 6) || (i > 18) ? hTStart.y + tiltedMarkersLength : hTStart.y - tiltedMarkersLength;
            }

            Point hTStop = new Point( (int) Math.round(xStop), (int) Math.round(yStop));
            List<Point> hourT = new ArrayList<>(Arrays.asList(hTStart, hTStop));
            markers.add(hourT);
        }
        return markers;
    }

    List<Point> getHourDotsCoordinates() {
        List<Point> dots = new ArrayList<>();
        for (int i = 0; i < hoursCoordinates.size(); i++) {
            if (i % 3 != 0) {
                dots.add(hoursCoordinates.get(i));
            }
        }
        return dots;
    }

    List<Point> getCurrentTimeHandCoordinates() {
        Calendar calendar = Calendar.getInstance();
        float hours = calendar.get(Calendar.HOUR_OF_DAY);
        float minutes = calendar.get(Calendar.MINUTE);
        float degrees = (hours + minutes / 60) * 15;
        Log.d(TAG, String.format("Time for hand drawing: %d:%d (%f degrees)", (int) hours, (int) minutes, degrees));

        Point handEnd = calculateCircumferencePoint(degrees);
        List<Point> handLine = new ArrayList<>(Arrays.asList(center, handEnd));
        calendar.get(Calendar.HOUR);
        return handLine;
    }

    List<Point> getDigitsCoordinates() {
        List<Point> digitsCoordinates = new ArrayList<>();
        for (int degree : degrees) {
//            some magic to make digits positions looks symmetric
            double padding = digitRadiusPadding;
            if (degree != 0 && degree <= 135) {
                degree += 1;
            }
            else if (degree >= 225) {
                degree -= 1;
            }
            if (degree <= 180) {
                padding = padding - (10 - (float) degree / 15);
            }
            else {
                padding = padding - (10 -  (360 - (float) degree) / 15);
            }
            digitsCoordinates.add(calculateConcentricPoint(degree, Math.round(radius + padding)));
        }
        return digitsCoordinates;
    }

    Point getDateCoordinates() {
        return new Point(dateXPadding, dateYPadding);
    }

    RectF getWidgetCircleObject() {
        RectF oval = new RectF();
        List<List<Point>> markers = getHourMarkersCoordinates();
        oval.set(markers.get(6).get(0).x,
                markers.get(0).get(0).y,
                markers.get(2).get(0).x,
                markers.get(4).get(0).y);
        return oval;
    }

    EventDegreeData getEventDegrees(Event event) {
        float startDegree = timeToDegree(event.getStartTime());
        float endDegree = timeToDegree(event.getFinishTime());

        float sweepDegree;
        if (startDegree == endDegree) { // event with zero duration
            sweepDegree = (float) 0.001;
        } else {
            sweepDegree = endDegree - startDegree;
            if (sweepDegree < -180) {
                sweepDegree += 360;
            }
        }

        return new EventDegreeData(startDegree, sweepDegree);
    }

    Point calculateEventTitlePoint(double degree) {
        return calculateConcentricPoint(degree, radius - markersLength);
    }


    private void calculateSizesAccordingToScreen(int side) {
        int padding = side / 13;
        borderWidth = side / 100;
        dotRadius = side / 100;
        smallDigitSize = side / 40;
        bigDigitSize = side / 25;
        dateSize = side / 18;
        markersLength = side / 36;
        dateXPadding = side / 36;
        dateYPadding = side / 15;
        titleSize = side / 27;

        handWidth = borderWidth / 2;
        tiltedMarkersLength = markersLength * 0.7;
        digitRadiusPadding = padding * 0.5;
        radius = side / 2 - padding;

        center = calculateWidgetCenter(screenSize, padding, radius, dateSize);
        hoursCoordinates = calculateHoursCoordinates();
    }

    private static Point calculateWidgetCenter(Point screenSize, int padding, float radius, int dateSize) {
        float yPosition = padding + radius + dateSize;
        yPosition = (yPosition > (float) screenSize.y / 2) ? (float) screenSize.y / 2 : yPosition;
        return new Point(screenSize.x / 2, Math.round(yPosition));
    }

    private float timeToDegree(String time) {
        String[] timeSplitted = time.split(":");
        return (Integer.valueOf(timeSplitted[0]) + (float) Integer.valueOf(timeSplitted[1]) / 60) * 15 - 90;
    }

    private Point calculateCircumferencePoint(double degree) {
        return calculateConcentricPoint(degree, radius);
    }

    private Point calculateConcentricPoint(double degree, float radius) {
        double radians = degree * Math.PI / 180;
        double x = center.x + radius * Math.sin(radians);
        double y = center.y - radius * Math.cos(radians);
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    private List<Point> calculateHoursCoordinates() {
        List<Point> hours = new ArrayList<>();
        for (int degree : degrees) {
            hours.add(calculateCircumferencePoint(degree));
        }
        return hours;
    }

    @AllArgsConstructor
    class EventDegreeData {
        @Getter private float start;
        @Getter private float sweep;
    }
    
}
