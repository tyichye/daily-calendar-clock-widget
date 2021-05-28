package com.miltolstoy.roundcalendar;

import android.support.annotation.NonNull;

import java.util.Locale;

import lombok.Getter;
import lombok.Setter;


public class CalendarInfo {
    @Getter private int id;
    @Getter private String account;
    @Getter private String name;
    @Setter @Getter private boolean selected = true;

    private static int ALL_CALENDARS_ID = -100;

    CalendarInfo(int id, String account, String name) {
        this.id = id;
        this.account = account;
        this.name = name;
    }

    CalendarInfo() {
        this(ALL_CALENDARS_ID, null, null);
    }

    @NonNull @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s - %s", account, name);
    }

    String toDebugString() {
        return String.format(Locale.getDefault(), "ID: %d; account: %s; name: %s", id, account, name);
    }

    boolean isAllItem() {
        return (id == ALL_CALENDARS_ID);
    }
}
