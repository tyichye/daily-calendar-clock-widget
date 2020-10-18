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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SpinnerAdapter extends ArrayAdapter<CalendarInfo> {

    private Context context;
    private List<CalendarInfo> items;
    private List<SpinnerItem> spinnerItems;
    private boolean isFromView = false;

    SpinnerAdapter(Context context, int resource, List<CalendarInfo> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        spinnerItems = new ArrayList<>();
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    @NonNull @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    Set<String> getSelectedCalendarIds() {
        Set<String> selectedIds = new HashSet<>();
        for (CalendarInfo info : items) {
            if (info.isAllItem()) {
                continue;
            }
            if (info.isSelected()) {
                selectedIds.add(Integer.toString(info.getId()));
            }
        }
        return selectedIds;
    }


    @SuppressLint("InflateParams")
    private View getCustomView(final int position, View convertView) {
        final SpinnerItem item;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, null);
            item = new SpinnerItem();
            item.textView = convertView.findViewById(R.id.text);
            item.checkBox = convertView.findViewById(R.id.checkbox);
            convertView.setTag(item);
        } else {
            item = (SpinnerItem) convertView.getTag();
        }

        CalendarInfo infoItem = items.get(position);
        String itemText = infoItem.isAllItem() ? "ALL" : infoItem.toString();
        item.textView.setText(itemText);
        item.checkBox.setTag(position);
        isFromView = true;
        item.checkBox.setChecked(infoItem.isSelected());
        isFromView = false;

        item.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isFromView) {
                    return;
                }
                CalendarInfo item = items.get(position);
                item.setSelected(isChecked);
                if (item.isAllItem()) {
                    for (SpinnerItem spinnerItem : spinnerItems) {
                        spinnerItem.checkBox.setChecked(isChecked);
                    }
                }
            }
        });

        spinnerItems.add(item);
        return convertView;
    }

    private static class SpinnerItem {
        private CheckBox checkBox;
        private TextView textView;
    }
}
