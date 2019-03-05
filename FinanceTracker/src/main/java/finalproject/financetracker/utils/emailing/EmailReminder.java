package finalproject.financetracker.utils.emailing;

import finalproject.financetracker.model.daos.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class EmailReminder extends Thread {
    private static final long REMINDER_INTERVAL = 1000 * 60 * 15; // 15 minutes
    @Autowired
    UserDao userDao;
    @Autowired
    MailUtil mailUtil;

    @Override
    public void run() {
        String subject = "Track your finances at our Finance Tracker";
        String message = "Hello,\nConsider coming back to the Finance Tracker to check the new changes.";
        while (true) {
            int offset = 0;
            List<Map<String, Object>> toBeNotified;
            toBeNotified = userDao.getEmailsToBeNotifiedByReminder();
            for (Map<String, Object> email : toBeNotified) {
                String recipientEmail = (String) email.get("email");
                System.out.println("-------------------- " + recipientEmail + " ----------------------");
                new Thread(()->mailUtil.sendSimpleMessage(recipientEmail, "noreply@traxter.com", subject, message));
            }
            try {
                System.out.println("--------------- REMINDER GOES TO SLEEP --------------");
                Thread.sleep(REMINDER_INTERVAL);
            } catch (InterruptedException ex) {
                System.out.println("Reminder interrupted while sleeping through interval: " + ex.getMessage());
            }
        }
    }
}
