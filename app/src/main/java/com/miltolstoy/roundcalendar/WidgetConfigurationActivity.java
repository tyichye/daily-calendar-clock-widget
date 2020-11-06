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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

import static com.miltolstoy.roundcalendar.Logging.TAG;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private final Object saveButtonLock = new Object();
    private boolean saveButtonLockNotified = false;

    static final int CALENDAR_PERMISSION_CODE = 10;

    private static final String preferencesName = "RoundCalendarPrefs";
    private static final String eventColorSettingName = "useCalendarEventColor";
    private static final String calendarIdsSettingName = "calendarIds";

    private SpinnerAdapter spinnerAdapter;

    private static TextView sleepStartTimeTextView;
    private static TextView sleepEndTimeTextView;


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

        List<CalendarInfo> calendars = CalendarAdapter.getCalendars(this);
        Spinner dropdown = findViewById(R.id.calendars_dropdown);
        calendars.add(0, new CalendarInfo()); // "ALL" item
        spinnerAdapter = new SpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, calendars);
        dropdown.setAdapter(spinnerAdapter);

        final Resources resources = getResources();

        sleepStartTimeTextView = findViewById(R.id.sleep_start_time_text);
        setSleepTimeInfo(sleepStartTimeTextView, resources.getInteger(R.integer.sleep_start_hours),
                resources.getInteger(R.integer.sleep_start_minutes));
        sleepEndTimeTextView = findViewById(R.id.sleep_end_time_text);
        setSleepTimeInfo(sleepEndTimeTextView, resources.getInteger(R.integer.sleep_end_hours),
                resources.getInteger(R.integer.sleep_end_minutes));

        Point widgetSize = getWidgetSize(appWidgetManager, appWidgetId);
        final int dayShift = 0;
        drawWidget(this, views, widgetSize, dayShift);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        new WaitForOptionsSaveThread(appWidgetId).start();
    }

    public static void drawWidget(Context context, RemoteViews views, Point widgetSize, int dayShift) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);

        Set<String> selectedCalendars = preferences.getStringSet(calendarIdsSettingName, null);
        CalendarAdapter calendarAdapter = new CalendarAdapter(context, selectedCalendars, dayShift);

        boolean useCalendarEventColor = preferences.getBoolean(eventColorSettingName, Boolean.TRUE);
        ClockView clockView = new ClockView(context, widgetSize, useCalendarEventColor,
                getSleepTimeInfo(sleepStartTimeTextView), getSleepTimeInfo(sleepEndTimeTextView));
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

        Set<String> selectedIds = spinnerAdapter.getSelectedCalendarIds();
        if (selectedIds.isEmpty()) {
            Log.e(TAG, "No selected calendars");
            Toast.makeText(this, "Please select at least one calendar to display", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Selected calendars:");
        for (String id : selectedIds) {
            Log.d(TAG, id);
        }

        SharedPreferences preferences = view.getContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(eventColorSettingName, useCalendarEventColor);
        editor.putStringSet(calendarIdsSettingName, selectedIds);
        editor.apply();

        synchronized (saveButtonLock) {
            saveButtonLock.notify();
            saveButtonLockNotified = true;
        }
    }

    public void onSleepStartTimeChanged(View view) {
        onTimepickerClicked(view, sleepStartTimeTextView);
    }

    public void onSleepEndTimeChanged(View view) {
        onTimepickerClicked(view, sleepEndTimeTextView);
    }

    public void onTimepickerClicked(View view, final TextView textView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.timepicker, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0 /*x*/, 0 /*y*/);

        final TimePicker timePicker = popupView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        TimeInfo timeInfo = getSleepTimeInfo(textView);
        timePicker.setHour(timeInfo.getHours());
        timePicker.setMinute(timeInfo.getMinutes());

        Button button = popupView.findViewById(R.id.saveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSleepTimeInfo(textView, timePicker.getHour(), timePicker.getMinute());
                popupWindow.dismiss();
            }
        });
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

    private String formatTimeValue(int value) {
        String s = String.valueOf(value);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    private void setSleepTimeInfo(TextView textView, int hours, int minutes) {
        String text = " " + formatTimeValue(hours) + ":" + formatTimeValue(minutes);
        textView.setText(text);
    }

    private static TimeInfo getSleepTimeInfo(TextView textView) {
        if (textView == null) {
            Log.e(TAG, "Text view is not initialized");
            return null;
        }
        String text = (String) textView.getText();
        String[] parsed = text.split(":");
        if (parsed.length != 2) {
            Log.e(TAG, "Invalid time info: " + text);
            return null;
        }
        return new TimeInfo(Integer.valueOf(parsed[0].trim()), Integer.valueOf(parsed[1].trim()));
    }
}
