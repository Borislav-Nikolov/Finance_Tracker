package finalproject.financetracker.model.utils.emailing;

import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Component
public class EmailSender {
    private static final long REMINDER_INTERVAL = TimeUnit.DAYS.toMillis(1); // 24 hours
    public static final Object reminderLock = new Object();
    @Autowired
    UserDao userDao;
    @Autowired
    MailUtil mailUtil;
    public void startReminder() {
        // TODO consider if not too slow
        Thread reminder = new Thread(() -> {
            String subject = "Track your finances at our Finance Tracker";
            String message = "Hello,\nConsider coming back to the Finance Tracker to check the new changes.";
            while (true) {
                ResultSet toBeNotified = null;
                try {
                    toBeNotified = userDao.getAllEmailsToBeNotifiedByReminder();
                    while (toBeNotified.next()) {
                        System.out.println("----------------- 3 -----------------");
                        // TODO consider extracting the user here to validate once again dates and subscription status
                        String recipientEmail = toBeNotified.getString("email");
                        // TODO consider if new Thread is really better than doing it on current thread
                        System.out.println(recipientEmail);
                        new Thread(() -> {
                            mailUtil.sendSimpleMessage(recipientEmail, "noreply@traxter.com", subject, message);
                            userDao.updateUserLastNotified(recipientEmail);
                        }).start();
                    }
                    Thread.sleep(100);
                    synchronized (reminderLock) {
                        reminderLock.notifyAll();
                    }
                } catch (SQLException | InterruptedException ex) {
                    System.out.println("Reminder failed: " + ex.getMessage());
                } finally {
                    try {
                        if (toBeNotified != null) {
                            toBeNotified.close();
                        }
                    } catch (SQLException ex) {
                        System.out.println("Failed to close ResultSet of toBeNotified users: " + ex.getMessage());
                    }
                }
                try {
                    Thread.sleep(REMINDER_INTERVAL);
                } catch (InterruptedException ex) {
                    System.out.println("Reminder interrupted while sleeping through interval: " + ex.getMessage());
                }
            }
        });
        reminder.setDaemon(true);
        reminder.start();
    }
}
