package com.miltolstoy.roundcalendar;

import android.text.format.DateUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class Rfc5545DurationTest {

    @Parameterized.Parameter()
    public String durationStr;

    @Parameterized.Parameter(1)
    public long durationMillis;

    @Parameterized.Parameters
    public static Collection parameters() {
        return Arrays.asList(new Object[][] {
                {null, 0},
                {"", 0},
                {"P2S", 2 * DateUtils.SECOND_IN_MILLIS},
                {"P3M", 3 * DateUtils.MINUTE_IN_MILLIS},
                {"P4H", 4 * DateUtils.HOUR_IN_MILLIS},
                {"P5D", 5 * DateUtils.DAY_IN_MILLIS},
                {"P6W", 6 * DateUtils.WEEK_IN_MILLIS},
                {"P11W12D13H14M15S", 11 * DateUtils.WEEK_IN_MILLIS + 12 * DateUtils.DAY_IN_MILLIS +
                    13 * DateUtils.HOUR_IN_MILLIS + 14 * DateUtils.MINUTE_IN_MILLIS + 15 * DateUtils.SECOND_IN_MILLIS}
        });
    }

    @Test
    public void durationToMillis() {
        assertEquals(Rfc5545Duration.toMilliSeconds(durationStr), durationMillis);
    }

}
