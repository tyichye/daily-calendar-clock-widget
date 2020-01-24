package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;

class EventInfo extends AppCompatTextView {

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
        String startTime = event.getStartTime();
        String finishTime = event.getFinishTime();
        String timeInfo;
        if (event.isFullDay()) {
            timeInfo = "full day";
        } else if (startTime.equals(finishTime)) {
            timeInfo = startTime;
        } else {
            timeInfo = String.format("%s-%s", startTime, finishTime);
        }

        String text = String.format("%s: %s", event.getTitle(), timeInfo);
        canvas.drawText(text, 40, 40, paint);
    }
}
