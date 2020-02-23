package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;


public class ClockView extends AppCompatImageView {

    private Map<String, Paint> paints;
    private ClockWidget clockWidget;
    private static final int backgroundColor = Color.TRANSPARENT;
    private static int refreshTimeoutMillis;
    private CalendarAdapter calendarAdapter = null;

    public ClockView(Context context) throws IllegalStateException {
        super(context);
        throw new IllegalStateException("Use another constructor");
    }

    public ClockView(Context context, Point screenSize) {
        super(context);
        clockWidget = new ClockWidget(screenSize);
        init(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        drawClock(canvas);
        drawDate(canvas);
        if (calendarAdapter != null) {
            drawEvents(canvas);
        }
        drawHand(canvas);

        postInvalidateDelayed(refreshTimeoutMillis);
    }

    void setCalendarAdapter(CalendarAdapter adapter) {
        calendarAdapter = adapter;
        invalidate();
    }


    private void init(Context context) {
        paints = initPaints();
        refreshTimeoutMillis = context.getResources().getInteger(R.integer.refreshPeriodMillis);
    }


    private Map<String, Paint> initPaints() {
        Map <String, Paint> paints = new HashMap<>();

        Paint dotsPaint = new Paint();
        dotsPaint.setColor(clockWidget.getBorderColor());
        paints.put("dots", dotsPaint);

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(clockWidget.getFillColor());
        paints.put("fill", fillPaint);

        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(clockWidget.getBorderColor());
        borderPaint.setStrokeWidth(clockWidget.getBorderWidth());
        paints.put("border", borderPaint);

        Paint handPaint = new Paint();
        handPaint.setColor(Color.RED);
        handPaint.setStrokeWidth(clockWidget.getHandWidth());
        paints.put("hand", handPaint);

        Paint smallDigitsPaint = new Paint();
        smallDigitsPaint.setTextSize(clockWidget.getSmallDigitSize());
        smallDigitsPaint.setTextAlign(Paint.Align.CENTER);
        smallDigitsPaint.setColor(clockWidget.getDigitColor());
        paints.put("smallDigits", smallDigitsPaint);

        Paint bigDigitsPaint = new Paint();
        bigDigitsPaint.setTextSize(clockWidget.getBigDigitSize());
        bigDigitsPaint.setTextAlign(Paint.Align.CENTER);
        bigDigitsPaint.setColor(clockWidget.getDigitColor());
        paints.put("bigDigits", bigDigitsPaint);

        Paint datePaint = new Paint();
        datePaint.setTextSize(clockWidget.getDateSize());
        datePaint.setTextAlign(Paint.Align.LEFT);
        datePaint.setColor(clockWidget.getDigitColor());
        paints.put("date", datePaint);

        Paint eventLinePaint = new Paint();
        eventLinePaint.setStrokeWidth(clockWidget.getHandWidth());
        eventLinePaint.setColor(Color.BLUE);
        eventLinePaint.setAlpha(100);
        eventLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        paints.put("eventLine", eventLinePaint);

        Paint sleepEventLinePaint = new Paint();
        sleepEventLinePaint.setStrokeWidth(1);
        sleepEventLinePaint.setColor(Color.GRAY);
        sleepEventLinePaint.setAlpha(100);
        sleepEventLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        paints.put("sleepEventLine", sleepEventLinePaint);

        Paint textTitlePaint = new Paint();
        textTitlePaint.setTextSize(clockWidget.getTitleSize());
        textTitlePaint.setColor(clockWidget.getEventTitleColor());
        paints.put("title", textTitlePaint);

        for (Paint p : paints.values()) {
            p.setAntiAlias(true);
        }

        return paints;
    }

    private void drawClock(Canvas canvas) {
//        body
        canvas.drawCircle(clockWidget.getCenter().x,
                clockWidget.getCenter().y,
                clockWidget.getRadius(),
                paints.get("fill"));
//        border
        canvas.drawCircle(clockWidget.getCenter().x,
                clockWidget.getCenter().y,
                clockWidget.getRadius(),
                paints.get("border"));
//        markers
        List<List<Point>> markers = clockWidget.getHourMarkersCoordinates();
        for (List<Point> marker : markers) {
            canvas.drawLine(marker.get(0).x, marker.get(0).y,
                    marker.get(1).x, marker.get(1).y,
                    paints.get("border"));
        }
//        dots
        List<Point> dots = clockWidget.getHourDotsCoordinates();
        for (Point dot : dots) {
            canvas.drawCircle(dot.x, dot.y, clockWidget.getDotRadius(), paints.get("dots"));
        }
//        digits
        List<Point> digits = clockWidget.getDigitsCoordinates();
        for (int i = 0; i < digits.size(); i++) {
            Paint paint;
            if (i % 3 == 0) {
                paint = paints.get("bigDigits");
            }
            else {
                paint = paints.get("smallDigits");
            }
            Point coords = digits.get(i);
            canvas.drawText(Integer.toString(i), coords.x, coords.y, paint);
        }
    }

    private void drawHand(Canvas canvas) {
        List<Point> hand = clockWidget.getCurrentTimeHandCoordinates();
        canvas.drawLine(hand.get(0).x, hand.get(0).y, hand.get(1).x, hand.get(1).y, paints.get("hand"));
        canvas.drawCircle(hand.get(0).x, hand.get(0).y, clockWidget.getDotRadius(), paints.get("dots"));
    }

    private void drawDate(Canvas canvas) {
        Point datePoint = clockWidget.getDateCoordinates();
        Calendar calendar = Calendar.getInstance();
        String date = String.format(Locale.US, "%2d.%2d.%d", calendar.get(Calendar.DAY_OF_MONTH),
                (calendar.get(Calendar.MONTH) + 1), calendar.get(YEAR)).replace(' ', '0');
        canvas.drawText(date, datePoint.x, datePoint.y, paints.get("date"));
    }

    private void drawEvents(Canvas canvas) {
        RectF widgetCircle = clockWidget.getWidgetCircleObject();

        for (Event event : getSleepEvents()) {
            ClockWidget.EventDegreeData degrees = clockWidget.getEventDegrees(event);
            canvas.drawArc(widgetCircle, degrees.getStart(), degrees.getSweep(), true, paints.get("sleepEventLine"));
        }

        for (Event event : calendarAdapter.getTodayEvents()) {
            if (event.isAllDay()) {
                continue;
            }
            ClockWidget.EventDegreeData degrees = clockWidget.getEventDegrees(event);
            canvas.drawArc(widgetCircle, degrees.getStart(), degrees.getSweep(), true, paints.get("eventLine"));

            canvas.save();
            Point eventTitlePoint = clockWidget.calculateEventTitlePoint(degrees.getStart() + 90);
            canvas.rotate(degrees.getStart() - 180, eventTitlePoint.x, eventTitlePoint.y);
            canvas.drawText(event.getTitle(), eventTitlePoint.x, eventTitlePoint.y, paints.get("title"));
            canvas.restore();
        }
    }

    private List<Event> getSleepEvents() {
        Calendar calendar = Calendar.getInstance();
        List<Event> events = new ArrayList<>();

        calendar.set(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), 21, 0, 0);
        long startTimeBeforeMidnight = calendar.getTimeInMillis();
        calendar.set(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), 23, 59, 59);
        long endTimeBeforeMidnight = calendar.getTimeInMillis();
        long beforeMidnightduration = endTimeBeforeMidnight - startTimeBeforeMidnight;
        events.add(new Event("sleep before midnight", startTimeBeforeMidnight, endTimeBeforeMidnight,
                beforeMidnightduration, false));

        calendar.set(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), 0, 0, 0);
        long startTimeAfterMidnight = calendar.getTimeInMillis();
        calendar.set(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), 6, 0, 0);
        long endTimeAfterMidnight = calendar.getTimeInMillis();
        long afterMidnightDuration = endTimeAfterMidnight - startTimeAfterMidnight;
        events.add(new Event("sleep after midnight", startTimeAfterMidnight, endTimeAfterMidnight,
                afterMidnightDuration, false));

        return events;
    }

}
