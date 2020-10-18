/*
Round Calendar
Copyright (C) 2020 Mil Tolstoy <miltolstoy@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
