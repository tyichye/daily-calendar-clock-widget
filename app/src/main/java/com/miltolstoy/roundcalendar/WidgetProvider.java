package com.miltolstoy.roundcalendar;

import android.app.Activity;
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
        int sideSize = widgetInfo.minHeight;

        CalendarAdapter calendarAdapter = new CalendarAdapter(context);
        ClockView clockView = new ClockView(context, sideSize / 5, new Point(sideSize / 4, sideSize / 4));
        clockView.setCalendarAdapter(calendarAdapter);

        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        clockView.draw(new Canvas(bitmap));
        views.setImageViewBitmap(R.id.widgetClockView, bitmap);
        appWidgetManager.updateAppWidget(appWidgetIds[0], views);

    }
}
