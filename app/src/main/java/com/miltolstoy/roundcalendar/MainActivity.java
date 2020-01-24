package com.miltolstoy.roundcalendar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "RoundCalendar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalendarAdapter calendarAdapter = new CalendarAdapter(this);
        calendarAdapter.requestCalendarPermissionsIfNeeded();

        ClockView clockView = findViewById(R.id.clockView);
        clockView.setLayoutParams(clockView.getLayoutParamsObject());
        clockView.setCalendarAdapter(calendarAdapter);

        EventsView eventsView = findViewById(R.id.eventsView);
        eventsView.setCalendarAdapter(calendarAdapter);
    }

}

