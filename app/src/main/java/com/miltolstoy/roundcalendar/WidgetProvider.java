package com.miltolstoy.roundcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Point;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        for (int id : appWidgetIds) {
            Point widgetSize = WidgetConfigurationActivity.getWidgetSize(context, appWidgetManager, id);
            WidgetConfigurationActivity.drawWidget(context, views, widgetSize);
            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
