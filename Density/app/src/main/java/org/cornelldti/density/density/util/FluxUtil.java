package org.cornelldti.density.density.util;

import android.content.res.Resources;

import org.cornelldti.density.density.R;

import java.text.SimpleDateFormat;
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

    // returns integer corresponding to day where 0 = Sunday, 1 = Monday, ... , 6 = Saturday
    public static int intOfDay(String day) {
        switch (day) {
            case "SUN":
                return 0;
            case "MON":
                return 1;
            case "TUE":
                return 2;
            case "WED":
                return 3;
            case "THU":
                return 4;
            case "FRI":
                return 5;
            case "SAT":
                return 6;
        }
        return -1;
    }

    public static String dateOfDay(String day, String pattern) {
        Calendar current = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        SimpleDateFormat checkFormat = new SimpleDateFormat("E");

        String dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        while (!dayCheck.equals(day)) {
            current.add(Calendar.DAY_OF_MONTH, 1);
            dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        }

        return format.format(current.getTime());
    }
}
