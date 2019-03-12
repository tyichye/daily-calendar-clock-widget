package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.TextView;

class EventInfo extends TextView {

    private Event event;
    private Paint paint;

    EventInfo(Context context, Event event) {
        super(context);
        this.event = event;
        this.paint = new Paint();
        paint.setTextSize(40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = String.format("%s: %s-%s", event.getTitle(), event.getStartTime(), event.getFinishTime());
        canvas.drawText(text, 40, 40, paint);
    }
}