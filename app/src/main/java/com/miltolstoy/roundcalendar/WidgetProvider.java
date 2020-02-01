package com.miltolstoy.roundcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.widget.RemoteViews;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;


public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetIds[0]);

        CalendarAdapter calendarAdapter = new CalendarAdapter(context);
        Point screenCenter = new Point(pxToDp(context, widgetInfo.minWidth),
                pxToDp(context, widgetInfo.minHeight));
        ClockView clockView = new ClockView(context, screenCenter);
        clockView.setCalendarAdapter(calendarAdapter);

        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        clockView.draw(new Canvas(bitmap));
        views.setImageViewBitmap(R.id.widgetClockView, bitmap);
        appWidgetManager.updateAppWidget(appWidgetIds[0], views);

    }

    private int pxToDp(Context context, int dp) {
        return dp / (int) context.getResources().getDisplayMetrics().density;
    }
}
