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

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RemoteViews;

import static com.miltolstoy.roundcalendar.Logging.TAG;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private final Object saveButtonLock = new Object();
    private boolean saveButtonLockNotified = false;

    static final int CALENDAR_PERMISSION_CODE = 10;

    private static final String preferencesName = "RoundCalendarPrefs";
    private static final String eventColorSettingName = "useCalendarEventColor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        requestCalendarPermissionsIfNeeded();

        final int appWidgetId = getAppWidgetId(getIntent());
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            sendResultAndExit(RESULT_CANCELED, appWidgetId);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);

        Point widgetSize = getWidgetSize(appWidgetManager, appWidgetId);
        drawWidget(this, views, widgetSize, 0);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        new WaitForOptionsSaveThread(appWidgetId).start();
    }

    public static void drawWidget(Context context, RemoteViews views, Point widgetSize, int dayShift) {
        CalendarAdapter calendarAdapter = new CalendarAdapter(context, CalendarAdapter.CALENDAR_EMPTY_ID, dayShift);
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        boolean useCalendarEventColor = preferences.getBoolean(eventColorSettingName, Boolean.TRUE);
        ClockView clockView = new ClockView(context, widgetSize, useCalendarEventColor);
        clockView.setCalendarAdapter(calendarAdapter);
        Bitmap bitmap = Bitmap.createBitmap(widgetSize.x, widgetSize.y, Bitmap.Config.ARGB_8888);
        clockView.draw(new Canvas(bitmap));
        views.setImageViewBitmap(R.id.widgetClockView, bitmap);
    }

    public static Point getWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId) {
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        Log.d(TAG, "Widget height: " + widgetInfo.minHeight + ", width: " + widgetInfo.minWidth);
        return new Point(widgetInfo.minWidth, widgetInfo.minHeight);
    }

    public void onSaveClicked(View view) {
        RadioButton calendarEventColorButton = findViewById(R.id.calendar_color_radio);
        boolean useCalendarEventColor = calendarEventColorButton.isChecked();
        Log.d(TAG, "Using " + (useCalendarEventColor ? "calendar" : "default") + " event color");

        SharedPreferences preferences = view.getContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(eventColorSettingName, useCalendarEventColor);
        editor.apply();

        synchronized (saveButtonLock) {
            saveButtonLock.notify();
            saveButtonLockNotified = true;
        }
    }

    void requestCalendarPermissionsIfNeeded() {
        Log.d(TAG, "Checking READ_CALENDAR permission");
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "READ_CALENDAR permission granted");
            return;
        }

        Log.d(TAG, "Requesting READ_CALENDAR permission");
        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, CALENDAR_PERMISSION_CODE);
        Log.e(TAG, "Calendar permission not granted");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        System.exit(0);
    }

    private int getAppWidgetId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "Empty extras");
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }
        return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private void sendResultAndExit(int result, int appWidgetId) {
        sendResultAndExit(result, null, appWidgetId);
    }

    private void sendResultAndExit(int result, String action, int appWidgetId) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        resultValue.setAction(action);
        setResult(result, resultValue);
        finish();
    }

    private class WaitForOptionsSaveThread extends Thread {

        private int appWidgetId;

        WaitForOptionsSaveThread(int widgetId) {
            appWidgetId = widgetId;
        }

        @Override
        public void run() {
            try {
                synchronized (saveButtonLock) {
                    if (!saveButtonLockNotified) {
                        saveButtonLock.wait();
                    }
                }
                sendResultAndExit(RESULT_OK, AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED, appWidgetId);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                sendResultAndExit(RESULT_CANCELED, appWidgetId);
            }
        }
    }
}
