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

import android.text.format.DateUtils;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class Rfc5545DurationTest {

    @RunWith(Parameterized.class)
    public static class DurationToMillisPositive {

        @Parameterized.Parameter()
        public String durationStr;

        @Parameterized.Parameter(1)
        public long durationMillis;

        @Parameterized.Parameters
        public static Collection parameters() {
            return Arrays.asList(new Object[][] {
                    {"P2S", 2 * DateUtils.SECOND_IN_MILLIS},
                    {"P3M", 3 * DateUtils.MINUTE_IN_MILLIS},
                    {"P4H", 4 * DateUtils.HOUR_IN_MILLIS},
                    {"P5D", 5 * DateUtils.DAY_IN_MILLIS},
                    {"P6W", 6 * DateUtils.WEEK_IN_MILLIS},
                    {"P11W12D13H14M15S", 11 * DateUtils.WEEK_IN_MILLIS + 12 * DateUtils.DAY_IN_MILLIS +
                            13 * DateUtils.HOUR_IN_MILLIS + 14 * DateUtils.MINUTE_IN_MILLIS + 15 * DateUtils.SECOND_IN_MILLIS},
                    {"P15DT5H0M20S", 15 * DateUtils.DAY_IN_MILLIS + 5 * DateUtils.HOUR_IN_MILLIS +
                            20 * DateUtils.SECOND_IN_MILLIS}, // rfc example
                    {"P7W", 7 * DateUtils.WEEK_IN_MILLIS}, // rfc example
            });
        }

        @Test
        public void test() {
            assertEquals(Rfc5545Duration.toMilliSeconds(durationStr), durationMillis);
        }
    }


    @RunWith(Parameterized.class)
    public static class DurationToMillisNegative {

        @Parameterized.Parameter()
        public String durationStr;

        @Parameterized.Parameters
        public static Collection parameters() {
            return Arrays.asList(
                    null,
                    "",
                    "1W",
                    "P",
                    "P1E"
            );
        }

        @Test(expected = IllegalArgumentException.class)
        public void test() {
            Rfc5545Duration.toMilliSeconds(durationStr);
        }
    }
}
