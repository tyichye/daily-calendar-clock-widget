package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ClockWidget extends Widget {

    static final int borderColor = Color.BLACK;
    static final int fillColor = Color.rgb(211, 211, 211);
    static final int borderWidth = 10;
    static final int handWidth = borderWidth / 2;
    static final int dotRadius = 8;
    static final int smallDigitSize = 25;
    static final int bigDigitSize = 40;
    static final int dateSize = 60;

    static private final String TAG = "ClockWidget";
    static private int[] degrees = {0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180, 195, 210, 225, 240, 255, 270, 285, 300, 315, 330, 345};
    static private final int markersLength = 30;
    static private final double tiltedMarkersLength = markersLength * 0.7;
    static private final int padding = 80;
    static private final double digitRadiusPadding = padding * 0.5;
    static private final int dateXPadding = 30;
    static private final int dateYPadding = 70;

    private Point center;
    private float radius;
    private List<Point> hoursCoordinates;

    ClockWidget (Context context) {
        super(context);
        radius = calculateWidgetRadius();
        center = calculateWidgetCenter();
        hoursCoordinates = calculateHoursCoordinates();
    }

    Point getCenter() {
        return center;
    }

    float getRadius() {
        return radius;
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
                padding = padding - (10 - degree / 15);
            }
            else {
                padding = padding - (10 - (360 - degree) / 15);
            }
            digitsCoordinates.add(calculateConcentricPoint(degree, Math.round(radius + padding)));
        }
        return digitsCoordinates;
    }

    Point getDateCoordinates() {
        return new Point(dateXPadding, dateYPadding);
    }

    List<Point> getDelimiterLineCoordinates() {
        Point maxPoint = getWidgetMaxPoint();
        return new ArrayList<>(Arrays.asList(new Point(0, maxPoint.y), maxPoint));
    }

    Point getWidgetMaxPoint() {
        return new Point(screenSize.x, (int) Math.round(hoursCoordinates.get(11).y + digitRadiusPadding + bigDigitSize));
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

    float[] getEventDegrees(Event event) {
        float[] degrees = new float[2];
        String[] eventTimes = {event.getStartTime(), event.getFinishTime()};
        for (int i = 0; i < eventTimes.length; i++) {
            String[] splitTime = eventTimes[i].split(":");
            degrees[i] = (Integer.valueOf(splitTime[0]) + (float) Integer.valueOf(splitTime[1]) / 60) * 15 - 90;
        }
        degrees[1] = degrees[1] - degrees[0];
        return degrees;
    }

    private Point calculateWidgetCenter() {
        float yPosition = padding + radius + dateSize;
        yPosition = yPosition > screenSize.y / 2 ? screenSize.y / 2 : yPosition;
        center = new Point(screenSize.x / 2, Math.round(yPosition));
        Log.d(TAG, String.format("Widget center: (%d, %d)", center.x, center.y));
        return center;
    }

    private float calculateWidgetRadius() {
        float smallerSide = screenSize.x < screenSize.y ? screenSize.x : screenSize.y;
        radius = smallerSide / 2 - padding;
        Log.d(TAG, String.format("Radius: %f", radius));
        return radius;
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
    
}
