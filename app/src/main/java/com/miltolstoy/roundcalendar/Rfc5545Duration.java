package com.miltolstoy.roundcalendar;

import android.text.format.DateUtils;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miltolstoy.roundcalendar.Logging.TAG;

class Rfc5545Duration {

    static long toMilliSeconds(String duration) {
        if (duration == null) {
            return 0;
        }

        duration = duration.substring(1); // remove "P" constant prefix
        Pattern pattern = Pattern.compile("(\\d+)([WDHMS])");
        Matcher matcher = pattern.matcher(duration);

        long milliSeconds = 0;
        while (matcher.find()) {
            milliSeconds += entryToMillis(Integer.parseInt(matcher.group(1)), matcher.group(2));
        }

        return milliSeconds;
    }

    private static long entryToMillis(int count, String dimension) {
        Map<String, Long> dimensionMap = new HashMap<String, Long>() {{
            put("W", DateUtils.WEEK_IN_MILLIS);
            put("D", DateUtils.DAY_IN_MILLIS);
            put("H", DateUtils.HOUR_IN_MILLIS);
            put("M", DateUtils.MINUTE_IN_MILLIS);
            put("S", DateUtils.SECOND_IN_MILLIS);
        }};

        Long millis = dimensionMap.get(dimension);
        if (millis == null) {
            Log.e(TAG, "Unknown dimension: " + dimension);
            return 0;
        }
        return count * millis;
    }

}
