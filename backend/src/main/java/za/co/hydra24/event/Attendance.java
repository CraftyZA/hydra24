package za.co.hydra24.event;

import za.co.hydra24.options.Option;

import java.util.ArrayList;
import java.util.List;

/** Which days of an event an entrant attends: ALL_DAYS, or DAY_1, DAY_2, ... */
public final class Attendance {

    private static final String ALL_DAYS = "ALL_DAYS";
    private static final String DAY_PREFIX = "DAY_";

    private Attendance() {
    }

    /** The choices at an event of this many days. "All days" means nothing at a one-day event. */
    static List<Option> optionsFor(int days) {
        List<Option> options = new ArrayList<>();
        if (days > 1) {
            options.add(new Option(ALL_DAYS, label(ALL_DAYS)));
        }
        for (int day = 1; day <= days; day++) {
            String value = DAY_PREFIX + day;
            options.add(new Option(value, label(value)));
        }
        return options;
    }

    public static String label(String attendance) {
        return ALL_DAYS.equals(attendance) ? "All days" : "Day " + attendance.substring(DAY_PREFIX.length());
    }
}
