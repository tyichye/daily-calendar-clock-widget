package com.miltolstoy.roundcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import static com.miltolstoy.roundcalendar.Logging.TAG;

public class WidgetConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        CalendarAdapter calendarAdapter = new CalendarAdapter(this);
        calendarAdapter.requestCalendarPermissionsIfNeeded();

        int appWidgetId = getAppWidgetId(getIntent());
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            sendResultAndExit(RESULT_CANCELED, appWidgetId);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);

        Point widgetSize = getWidgetSize(this, appWidgetManager, appWidgetId);
        drawWidget(this, views, widgetSize);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        sendResultAndExit(RESULT_OK, appWidgetId);
    }


    public static void drawWidget(Context context, RemoteViews views, Point widgetSize) {
        CalendarAdapter calendarAdapter = new CalendarAdapter(context);
        ClockView clockView = new ClockView(context, widgetSize);
        clockView.setCalendarAdapter(calendarAdapter);
        Bitmap bitmap = Bitmap.createBitmap(widgetSize.x, widgetSize.y, Bitmap.Config.ARGB_8888);
        clockView.draw(new Canvas(bitmap));
        views.setImageViewBitmap(R.id.widgetClockView, bitmap);
    }

    public static Point getWidgetSize(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int width = pxToDp(context, widgetInfo.minWidth);
        int height = pxToDp(context, widgetInfo.minHeight);
        Log.d(TAG, "Widget height: " + height + ", width: " + width);
        return new Point(width, height);
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
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(result, resultValue);
        finish();
    }

    private static int pxToDp(Context context,int dp) {
        return dp / (int) context.getResources().getDisplayMetrics().density;
    }
}
