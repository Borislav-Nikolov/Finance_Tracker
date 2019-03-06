package finalproject.financetracker.utils.emailing;

import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.pojos.Budget;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.pojos.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;

@Component
public class EmailSender {
    private static final long REMINDER_INITIAL_DELAY = 1000 * 60 * 5; // 5 minutes
    private static final long REMINDER_INTERVAL = 1000 * 60 * 15; // 15 minutes
    @Autowired
    UserDao userDao;
    @Autowired
    MailUtil mailUtil;

    @Scheduled(initialDelay = REMINDER_INITIAL_DELAY, fixedDelay = REMINDER_INTERVAL)
    public void sendReminders() {
        new Thread(()->{
            String subject = "Track your finances at our Finance Tracker";
            String message = "Hello,\nConsider coming back to the Finance Tracker to check the new changes.";
            List<Map<String, Object>> toBeNotified;
            toBeNotified = userDao.getEmailsToBeNotifiedByReminder();
            for (Map<String, Object> email : toBeNotified) {
                String recipientEmail = (String) email.get("email");
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

    public void sendEmailConfirmationToken(String appUrl, VerificationToken verToken, User user) {
        if (user.isEmailConfirmed()) {
            throw new EmailAlreadyConfirmedException();
        }
        String recipientAddress = user.getEmail();
        String token = verToken.getToken();
        String subject = "Confirm Email";
        String confirmationUrl = "http://localhost:8888" + appUrl + "/confirm?token=" + token;
        String message = "Please, click the following link to confirm your email:\n" + confirmationUrl;
        new Thread(() -> mailUtil.sendSimpleMessage(recipientAddress, "noreply@traxter.com", subject, message))
                .start();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST) // 400
    private static class EmailAlreadyConfirmedException extends RuntimeException {
        private EmailAlreadyConfirmedException() {
            super("Email has already been confirmed.");
        }
    }

}
