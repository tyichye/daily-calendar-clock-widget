package com.miltolstoy.roundcalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.widget.RemoteViews;

import static com.miltolstoy.roundcalendar.Logging.TAG;


public class WidgetProvider extends AppWidgetProvider {

    private static final String previousDayAction = "previousDayAction";
    private static final String nextDayAction = "nextDayAction";
    private static final String todayAction = "todayAction";

    private static int daysShift = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.d(TAG, "Empty action");
            return;
        }

        if (!action.equals(previousDayAction) && !action.equals(nextDayAction) && !action.equals(todayAction)
                && !action.equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
            Log.d(TAG, "Unhandled action: " + action);
            super.onReceive(context, intent);
            return;
        }

        if (action.equals(previousDayAction)) {
            daysShift -= 1;
        } else if (action.equals(nextDayAction)) {
            daysShift += 1;
        } else {
            daysShift = 0;
        }

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        drawAndUpdate(context, widgetId);

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            drawAndUpdate(context, id);
            setOnClickButtonsIntents(context, id);
        }
    }

    private static void setOnClickIntent(Context context, RemoteViews views, int widgetId, int viewId,
                                         String intentAction) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(intentAction);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(viewId, pendingIntent);
    }

    private static void drawAndUpdate(Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Point widgetSize = WidgetConfigurationActivity.getWidgetSize(context, appWidgetManager, widgetId);
        WidgetConfigurationActivity.drawWidget(context, views, widgetSize, daysShift);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    private static void setOnClickButtonsIntents(Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        setOnClickIntent(context, views, widgetId, R.id.previous_button, previousDayAction);
        setOnClickIntent(context, views, widgetId, R.id.next_button, nextDayAction);
        setOnClickIntent(context, views, widgetId, R.id.today_button, todayAction);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, views);
    }
}
