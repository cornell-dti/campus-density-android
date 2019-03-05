package org.cornelldti.density.density.util;

import android.content.res.Resources;

import org.cornelldti.density.density.R;

import java.util.Calendar;

public class FluxUtil {

    public static String getDayString() {
        String dayString = "";
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "SUN";
                break;
            case Calendar.MONDAY:
                dayString = "MON";
                break;
            case Calendar.TUESDAY:
                dayString = "TUE";
                break;
            case Calendar.WEDNESDAY:
                dayString = "WED";
                break;
            case Calendar.THURSDAY:
                dayString = "THU";
                break;
            case Calendar.FRIDAY:
                dayString = "FRI";
                break;
            case Calendar.SATURDAY:
                dayString = "SAT";
                break;
        }
        return dayString;
    }

    public static String dayFullString(String day) {
        switch (day) {
            case "MON":
                return "Monday";
            case "TUE":
                return "Tuesday";
            case "WED":
                return "Wednesday";
            case "THU":
                return "Thursday";
            case "FRI":
                return "Friday";
            case "SAT":
                return "Saturday";
            case "SUN":
                return "Sunday";
        }
        return "";
    }
}
