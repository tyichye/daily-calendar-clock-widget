/*
Round Calendar
Copyright (C) 2020 Mil Tolstoy <miltolstoy@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.miltolstoy.roundcalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.miltolstoy.roundcalendar.Logging.TAG;


public class WidgetProvider extends AppWidgetProvider {

    private static final String previousDayAction = "previousDayAction";
    private static final String nextDayAction = "nextDayAction";
    private static final String todayAction = "todayAction";

    private static int daysShift = 0;
    private static Timer timer = new Timer();
    private static List<Integer> timerWidgetIds = new ArrayList<>();
    private static final long timerUpdatePeriodMilliseconds = 5 * 60 * 1000; // 5 minutes

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
            if (!timerWidgetIds.contains(id)) {
                timer.schedule(new WidgetUpdateTask(context, id), new Date(), timerUpdatePeriodMilliseconds);
                timerWidgetIds.add(id);
            }
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

    private static class WidgetUpdateTask extends TimerTask {

        private Context context;
        private int widgetId;

        WidgetUpdateTask(Context context, int widgetId) {
            Log.d(TAG, "Widget update task created for widget id: " + widgetId);
            this.context = context;
            this.widgetId = widgetId;
        }

        public void run() {
            drawAndUpdate(context, widgetId);
        }
    }
}
