package utils.helpers;

import lombok.extern.java.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Log
public class DateHelper {

    private static final String TIME_ZONE_EUROPE_KIEV = "Europe/Kiev";

    public static String getToday(String targetDatePattern) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(targetDatePattern));
    }

    public static String addDaysToLocalDate(String datePattern, String dateForAdding, int daysCount) {
        LocalDate targetDate = formatToLocalDateFromStringByPattern(datePattern, dateForAdding);

        return targetDate.plusDays(daysCount).format(DateTimeFormatter.ofPattern(datePattern));
    }

    private static LocalDate formatToLocalDateFromStringByPattern(String datePattern, String formattingDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        try {
            return formatter.parse(formattingDate)
                    .toInstant()
                    .atZone(ZoneId.of(TIME_ZONE_EUROPE_KIEV))
                    .toLocalDate();
        } catch (ParseException exception) {
            logger.warning(exception.getMessage());
        }
        throw new RuntimeException("Date formatting was failed");
    }
}
