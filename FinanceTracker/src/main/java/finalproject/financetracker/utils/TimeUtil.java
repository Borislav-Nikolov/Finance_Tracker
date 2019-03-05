package finalproject.financetracker.utils;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Component
public class TimeUtil {
    public Date getDateByMonthChange(int constant) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MONTH, constant);
        return new Date(cal.getTime().getTime());
    }
}
