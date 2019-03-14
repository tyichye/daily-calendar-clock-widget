package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

abstract class Widget {

    private String tag = "Widget";
    Point screenSize;

    Widget(Context context) {
        screenSize = getScreenSize(context);
    }

    private Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.d(tag, String.format("Screen size: (%d, %d)", size.x, size.y));
        return size;
    }
}
