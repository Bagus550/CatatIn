package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatter {

    private static final SimpleDateFormat viewFormat =
            new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm");

    public static String format(Date date) {
        if (date == null) return "";
        return viewFormat.format(date);
    }
}
