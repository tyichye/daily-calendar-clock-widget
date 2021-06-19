package com.opensource.roundcalendar;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.opensource.roundcalendar.R;

import java.util.Calendar;

import static com.opensource.roundcalendar.Logging.TAG;


public class WidgetProvider extends AppWidgetProvider {

    private static final String previousDayAction = "previousDayAction";
    private static final String nextDayAction = "nextDayAction";
    private static final String todayAction = "todayAction";
    private static final String tickAction = "com.miltolstoy.roundcalendar.clockTickAction";
    private static final String openCalendarAction = "openCalendarAction";

    // If widget update will be too frequent, Android will block it at all. If widget update period will be large, it
    // will affect user experience. Recommended value >= 1 minute.
    private static int updatePeriodMillis = 0;
    private static final Object updatePeriodLock = new Object();

    private static int daysShift = 0;

    @Override
    public void onEnabled(Context context)
    {
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            setupNextClockTick(context);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.d(TAG, "Empty action");
            return;
        }

        if (action.equals(openCalendarAction)){
            WidgetConfigurationActivity.onClockClicked(context, daysShift);
            return;
        }

        if (action.equals(tickAction)) {
            setupNextClockTick(context);
            Intent updateIntent = new Intent(context, WidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(updateIntent);
            super.onReceive(context, intent);
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


    public static void setUpdatePeriod(int value) {
        synchronized (updatePeriodLock) {
            updatePeriodMillis = value;
        }
    }

    private int getUpdatePeriod() {
        synchronized (updatePeriodLock) {
            return updatePeriodMillis;
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
        Point widgetSize = WidgetConfigurationActivity.getWidgetSize(appWidgetManager, widgetId);
        WidgetConfigurationActivity.drawWidget(context, views, widgetSize, daysShift);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    // set the behavior of the buttons - NEXT DAT, PREVIOUS DAY, TODAY DAY
    // the buttons appears as <  T  >
    public static void setOnClickButtonsIntents(Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        setOnClickIntent(context, views, widgetId, R.id.previous_button, previousDayAction);
        setOnClickIntent(context, views, widgetId, R.id.next_button, nextDayAction);
        setOnClickIntent(context, views, widgetId, R.id.today_button, todayAction);
        setOnClickIntent(context, views, widgetId, R.id.dateView, openCalendarAction);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, views);

    }

    private void setupNextClockTick(Context context) {
        final int updatePeriod = getUpdatePeriod();
        if (updatePeriod == 0) {
            Log.d(TAG, "Widget auto-update is disabled");
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, updatePeriod);
        Intent tickIntent = new Intent(context, WidgetProvider.class);
        tickIntent.setAction(tickAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, tickIntent, 0);
        alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }
}
