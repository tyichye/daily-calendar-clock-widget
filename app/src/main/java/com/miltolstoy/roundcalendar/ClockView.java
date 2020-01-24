package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;

public class ClockView extends View {

    private Paint paint;
    private Map<String, Paint> paints;
    private ClockWidget clockWidget;
    private static final int backgroundColor = Color.WHITE;
    private static final int refreshTimeout = 5 * 60 * 1000;
    private final int delimiterWidth = 5;
    private CalendarAdapter calendarAdapter = null;

    ClockView(Context context) {
        super(context);
        init(context);
    }

    ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        drawClock(canvas);
        drawDate(canvas);
        if (calendarAdapter != null) {
            drawDelimiter(canvas);
            drawEvents(canvas);
        }
        drawHand(canvas);

        postInvalidateDelayed(refreshTimeout);
    }

    void setCalendarAdapter(CalendarAdapter adapter) {
        calendarAdapter = adapter;
        invalidate();
    }

    LinearLayout.LayoutParams getLayoutParamsObject() {
        Point maxPoint = clockWidget.getWidgetMaxPoint();
        return new LinearLayout.LayoutParams(maxPoint.x, maxPoint.y + delimiterWidth);
    }

    private void init(Context context) {
        paint = new Paint();
        clockWidget = new ClockWidget(context);
        paints = initPaints();
    }

    private Map<String, Paint> initPaints() {
        Map <String, Paint> paints = new HashMap<>();

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(ClockWidget.fillColor);
        paints.put("fill", fillPaint);

        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(ClockWidget.borderColor);
        borderPaint.setStrokeWidth(ClockWidget.borderWidth);
        paints.put("border", borderPaint);

        Paint handPaint = new Paint();
        handPaint.setColor(Color.RED);
        handPaint.setStrokeWidth(ClockWidget.handWidth);
        paints.put("hand", handPaint);

        Paint smallDigitsPaint = new Paint();
        smallDigitsPaint.setTextSize(ClockWidget.smallDigitSize);
        smallDigitsPaint.setTextAlign(Paint.Align.CENTER);
        paints.put("smallDigits", smallDigitsPaint);

        Paint bigDigitsPaint = new Paint();
        bigDigitsPaint.setTextSize(ClockWidget.bigDigitSize);
        bigDigitsPaint.setTextAlign(Paint.Align.CENTER);
        paints.put("bigDigits", bigDigitsPaint);

        Paint datePaint = new Paint();
        datePaint.setTextSize(ClockWidget.dateSize);
        datePaint.setTextAlign(Paint.Align.LEFT);
        paints.put("date", datePaint);

        Paint delimiterPaint = new Paint();
        delimiterPaint.setStrokeWidth(delimiterWidth);
        paints.put("delimiter", delimiterPaint);

        Paint eventLinePaint = new Paint();
        eventLinePaint.setStrokeWidth(ClockWidget.handWidth);
        eventLinePaint.setColor(Color.BLUE);
        eventLinePaint.setAlpha(100);
        eventLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        paints.put("eventLine", eventLinePaint);

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
            canvas.drawCircle(dot.x, dot.y, ClockWidget.dotRadius, paint);
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
        canvas.drawCircle(hand.get(0).x, hand.get(0).y, ClockWidget.dotRadius, paint);
    }

    private void drawDate(Canvas canvas) {
        Point datePoint = clockWidget.getDateCoordinates();
        Calendar calendar = Calendar.getInstance();
        canvas.drawText(String.format(Locale.US, "%2d.%2d.%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)).replace(' ', '0'),
                datePoint.x, datePoint.y, paints.get("date"));
    }

    private void drawDelimiter(Canvas canvas) {
        List<Point> line = clockWidget.getDelimiterLineCoordinates();
        canvas.drawLine(line.get(0).x, line.get(0).y, line.get(1).x, line.get(1).y, paints.get("delimiter"));
    }

    private void drawEvents(Canvas canvas) {
        List<Event> todayEvents = calendarAdapter.getTodayEvents();
        RectF widgetCircle = clockWidget.getWidgetCircleObject();
        for (Event event : todayEvents) {
            float[] degrees = clockWidget.getEventDegrees(event);
            canvas.drawArc(widgetCircle, degrees[0], degrees[1], true, paints.get("eventLine"));
        }
    }

}
