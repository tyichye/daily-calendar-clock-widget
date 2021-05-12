package com.miltolstoy.roundcalendar;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class my_activity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_layout);

        Paint paint = new Paint();
        final RectF rect = new RectF();
        //Example values

        rect.set(20/2- 5, 20/2 - 5, 20/2 + 5, 20/2 + 5);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(20);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        Canvas canvas = new Canvas();
        canvas.drawArc(rect, -90, 360, false, paint);
    }


}
