package com.miltolstoy.roundcalendar;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.miltolstoy.roundcalendar.Logging.TAG;
import static java.util.Calendar.YEAR;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private final Object saveButtonLock = new Object();
    private boolean saveButtonLockNotified = false;

    static final int CALENDAR_PERMISSION_CODE = 10;

    private static final String preferencesName = "RoundCalendarPrefs";
    private static final String eventColorSettingName = "useCalendarEventColor";
    private static final String calendarIdsSettingName = "calendarIds";

    private SpinnerAdapter spinnerAdapter;

    private Switch autoUpdateSwitch;
    private EditText updatePeriodEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_configuration_layout);

        requestCalendarPermissionsIfNeeded();

        final int appWidgetId = getAppWidgetId(getIntent());
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            sendResultAndExit(RESULT_CANCELED, appWidgetId);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);

        // get calendars from googleCalendar and show them as list in the configuration screen
        // to give the user options which calendars he want to sync
        List<CalendarInfo> calendars = CalendarAdapter.getCalendars(this);
        Spinner dropdown = findViewById(R.id.calendars_dropdown);
        calendars.add(0, new CalendarInfo()); // "ALL" item
        spinnerAdapter = new SpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, calendars);
        dropdown.setAdapter(spinnerAdapter);

        updatePeriodEditText = findViewById(R.id.update_period);

        autoUpdateSwitch = findViewById(R.id.auto_update);

        autoUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (!b){
                    updatePeriodEditText.setEnabled(false);
                    updatePeriodEditText.setText("0");
                }
                else {
                    updatePeriodEditText.setEnabled(true);
                    updatePeriodEditText.setText("30");
                }

            }
        });


        Point widgetSize = getWidgetSize(appWidgetManager, appWidgetId);
        final int dayShift = 0;
        drawWidget(this, views, widgetSize, dayShift);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // wait for the user to click on the save button -> when click the app will close and the
        // widget will appear
        new WaitForOptionsSaveThread(appWidgetId).start();
    }

    public static void drawWidget(Context context, RemoteViews views, Point widgetSize, int dayShift) {
        // holds the preferences of the widget - which calendars to show, which colors to use
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);

        Set<String> selectedCalendars = preferences.getStringSet(calendarIdsSettingName, null);
        CalendarAdapter calendarAdapter = new CalendarAdapter(context, selectedCalendars, dayShift);

        boolean useCalendarEventColor = preferences.getBoolean(eventColorSettingName, Boolean.TRUE);

        // create clockView object with the relevant preferences - size of widget, color to use, etc
        ClockView clockView = new ClockView(context, widgetSize, useCalendarEventColor);
        clockView.setCalendarAdapter(calendarAdapter);

        Bitmap bitmap = Bitmap.createBitmap(widgetSize.x, widgetSize.y, Bitmap.Config.ARGB_8888);
        clockView.draw(new Canvas(bitmap));


        // date view
        Calendar calendar = calendarAdapter.getDayStartCalendar();
        String date = String.format(Locale.US, "%2d.%2d.%d", calendar.get(Calendar.DAY_OF_MONTH),
                (calendar.get(Calendar.MONTH) + 1), calendar.get(YEAR)).replace(' ', '0');

        views.setTextViewText(R.id.dateView, date);
        // end date view
        views.setImageViewBitmap(R.id.widgetClockView, bitmap);

    }

    public static Point getWidgetSize(AppWidgetManager appWidgetManager, int appWidgetId) {
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        Log.d(TAG, "Widget height: " + widgetInfo.minHeight + ", width: " + widgetInfo.minWidth);
        return new Point(widgetInfo.minWidth, widgetInfo.minHeight);
    }


    // this function run when the user click on the save button - according to XML file
    // activity_widget_configuration -> last view -> onClick = onSaveClicked
    public void onSaveClicked(View view) {

        // retrieve the user input for the widget preference:
        // which calendars to show, which color to use, update period
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

        int updatePeriod = 0;
        if (autoUpdateSwitch.isChecked()) {
            EditText updatePeriodEditText = findViewById(R.id.update_period);
            String updatePeriodString = updatePeriodEditText.getText().toString();
            if (updatePeriodString.isEmpty()) {
                Log.e(TAG, "Widget update period not specified");

                // Toast help us to show a small message in the screen for a few second
                // when user forgot to fill the update period we show him the following message
                Toast.makeText(this, "Please specify widget update period", Toast.LENGTH_LONG).show();
                return;
            }
            updatePeriod = Integer.parseInt(updatePeriodString);
        }
        Log.d(TAG, "Widget update period: " + updatePeriod);
        WidgetProvider.setUpdatePeriod(updatePeriod);

        // those lines update the preference of the widget according to user's input
        // the editor allow us to update the sharedPreference object efficiently
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

    public static void onClockClicked(Context context, int daysShift) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        calendar.add(Calendar.DAY_OF_MONTH, daysShift);

        long startMillis = calendar.getTimeInMillis();

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }


}
