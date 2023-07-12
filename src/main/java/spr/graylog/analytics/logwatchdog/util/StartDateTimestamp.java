package spr.graylog.analytics.logwatchdog.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StartDateTimestamp {
    public static LocalDateTime getStartTimestamp() {
        String dateString = "2023-06-23T00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    public static LocalDateTime getMlModelStartTimestamp() {
        String dateString = "2023-06-27T00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    private StartDateTimestamp() {

    }
}
