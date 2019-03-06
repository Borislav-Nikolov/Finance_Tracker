package finalproject.financetracker.utils;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

@Component
public class TimeUtil {
    private static final String[] DATE_FORMATS = {
            "dd MM yyyy", "dd.MM.yyyy", "dd-MM-yyyy", "dd/MM/yyyy",
            "M dd yyyy", "M.dd.yyyy", "M-dd-yyyy", "M/dd/yyyy",
            "yyyy MM dd", "yyyy.MM.dd", "yyyy-MM-dd", "yyyy/MM/dd"};

    public LocalDate checkParseLocalDate(String dateText) throws InvalidRequestDataException {
        for (String format : DATE_FORMATS) {
            try {
                return LocalDate.parse(dateText, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException ex) {/**/}
        }
        throw new InvalidRequestDataException("Date format is not correct.");
    }

    public Date getDateByMonthChange(int constant) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MONTH, constant);
        return new Date(cal.getTime().getTime());
    }
}
