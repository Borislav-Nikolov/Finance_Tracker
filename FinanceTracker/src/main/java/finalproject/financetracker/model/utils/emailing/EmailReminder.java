package finalproject.financetracker.model.utils.emailing;

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
    private static final long REMINDER_INTERVAL = TimeUnit.DAYS.toMillis(1); // 24 hours
    private static final int MAX_EMAILS_LIMIT = 2;
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
            while (true) {
                toBeNotified = userDao.getEmailsToBeNotifiedByReminder(MAX_EMAILS_LIMIT, offset);
                // TODO solve this:
//                    offset += MAX_EMAILS_LIMIT;
                for (Map<String, Object> email : toBeNotified) {
                    String recipientEmail = (String) email.get("email");
                    System.out.println("-------------------- " + recipientEmail + " ----------------------");
                    mailUtil.sendSimpleMessage(recipientEmail, "noreply@traxter.com", subject, message);
                    try {
                        System.out.println("------------------- SLEEPS -------------------");
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        System.out.println("opa: " + ex.getMessage());
                    }
                }
                if (toBeNotified.size() < MAX_EMAILS_LIMIT) {
                    toBeNotified = null;
                    break;
                }
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
