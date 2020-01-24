package com.miltolstoy.roundcalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;

class EventsView extends ScrollView {

    private CalendarAdapter calendarAdapter;

    EventsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setCalendarAdapter(CalendarAdapter adapter) {
        calendarAdapter = adapter;
        initEventsInfo(getContext());
    }

    void initEventsInfo(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        List<Event> todayEvents = calendarAdapter.getTodayEvents();
        for (Event event : todayEvents) {
            EventInfo info = new EventInfo(context, event);
            linearLayout.addView(info);
        }
        addView(linearLayout);
    }

}
