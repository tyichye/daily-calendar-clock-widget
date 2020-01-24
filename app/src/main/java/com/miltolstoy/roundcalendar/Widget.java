package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import static com.miltolstoy.roundcalendar.MainActivity.TAG;

abstract class Widget {

    Point screenSize;

    Widget(Context context) {
        screenSize = getScreenSize(context);
    }

    private Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.d(TAG, String.format("Screen size: (%d, %d)", size.x, size.y));
        return size;
    }
}
