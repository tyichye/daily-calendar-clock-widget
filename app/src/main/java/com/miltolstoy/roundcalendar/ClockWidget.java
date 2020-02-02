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

import lombok.Getter;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;

class ClockWidget {

    @Getter private final int borderColor = Color.BLACK;
    @Getter private final int fillColor = Color.rgb(211, 211, 211);
    private final int[] degrees = {0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180, 195, 210, 225, 240, 255, 270, 285, 300, 315, 330, 345};

    @Getter private int borderWidth;
    @Getter private int handWidth;
    @Getter private int dotRadius;
    @Getter private int smallDigitSize;
    @Getter private int bigDigitSize;
    @Getter private int dateSize;
    @Getter private int titleSize;
    private int markersLength;
    private double tiltedMarkersLength;
    private int padding;
    private double digitRadiusPadding;
    private int dateXPadding;
    private int dateYPadding;

    @Getter private Point center;
    @Getter private float radius;
    private Point screenSize;
    private List<Point> hoursCoordinates;

    ClockWidget(Context context) {
        this(getScreenSize(context));
    }

    ClockWidget(Point screenSize) {
        this.screenSize = screenSize;
        int minSide = (screenSize.x < screenSize.y) ? screenSize.x : screenSize.y;
        calculateSizesAccordingToScreen(minSide);

        radius = calculateWidgetRadius();
        center = calculateWidgetCenter();
        hoursCoordinates = calculateHoursCoordinates();
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

    EventDegreeData getEventDegrees(Event event) {
        float startDegree = timeToDegree(event.getStartTime());
        float endDegree = timeToDegree(event.getFinishTime());

        float sweepDegree;
        if (startDegree == endDegree) { // event with zero duration
            sweepDegree = (float) 0.001;
        } else {
            sweepDegree = endDegree - startDegree;
        }

        return new EventDegreeData(startDegree, sweepDegree);
    }

    Point calculateEventTitlePoint(double degree) {
        return calculateConcentricPoint(degree, radius - markersLength);
    }


    private static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.d(TAG, String.format("Screen size: (%d, %d)", size.x, size.y));
        return size;
    }

    private void calculateSizesAccordingToScreen(int side) {
        borderWidth = side / 100;
        handWidth = borderWidth / 2;
        dotRadius = side / 100;
        smallDigitSize = side / 40;
        bigDigitSize = side / 25;
        dateSize = side / 18;
        markersLength = side / 36;
        tiltedMarkersLength = markersLength * 0.7;
        padding = side / 13;
        digitRadiusPadding = padding * 0.5;
        dateXPadding = side / 36;
        dateYPadding = side / 15;
        titleSize = side / 27;
    }

    private float timeToDegree(String time) {
        String[] timeSplitted = time.split(":");
        return (Integer.valueOf(timeSplitted[0]) + (float) Integer.valueOf(timeSplitted[1]) / 60) * 15 - 90;
    }

    private Point calculateWidgetCenter() {
        float yPosition = padding + radius + dateSize;
        yPosition = yPosition > (float) screenSize.y / 2 ? (float) screenSize.y / 2 : yPosition;
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

    class EventDegreeData {

        float start;
        float sweep;

        EventDegreeData(float startDegree, float sweepDegree) {
            this.start = startDegree;
            this.sweep = sweepDegree;
        }
    }
    
}
