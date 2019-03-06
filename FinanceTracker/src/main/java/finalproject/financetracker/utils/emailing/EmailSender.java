package finalproject.financetracker.utils.emailing;

import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.pojos.Budget;
import finalproject.financetracker.model.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class EmailSender {
    private static final long REMINDER_INTERVAL = 1000 * 60 * 15; // 15 minutes
    @Autowired
    UserDao userDao;
    @Autowired
    MailUtil mailUtil;

    @Scheduled(fixedDelay = REMINDER_INTERVAL)
    public void sendReminders() {
        new Thread(()->{
            String subject = "Track your finances at our Finance Tracker";
            String message = "Hello,\nConsider coming back to the Finance Tracker to check the new changes.";
            List<Map<String, Object>> toBeNotified;
            toBeNotified = userDao.getEmailsToBeNotifiedByReminder();
            for (Map<String, Object> email : toBeNotified) {
                String recipientEmail = (String) email.get("email");
                System.out.println("-------------------- " + recipientEmail + " notified ----------------------");
                new Thread(()->mailUtil.sendSimpleMessage(recipientEmail, "noreply@traxter.com", subject, message))
                .start();
            }
        userDao.updateUsersLastNotified(toBeNotified);
        }).start();
    }

    public void sendBudgetNearLimitEmail(User user, Budget budget) {
        String subject = "Budget notification from Traxter";
        String message = user.getUsername() + ",\n" +
                "Your " + budget.getBudgetName() + " budget is at " + budget.getAmount() + ".";
        new Thread(()->
                mailUtil.sendSimpleMessage(user.getEmail(), "noreply@traxter.com", subject, message));
    }

}
