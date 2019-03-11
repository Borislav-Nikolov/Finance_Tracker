package finalproject.financetracker.utils.emailing;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.exceptions.UnauthorizedAccessException;
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

    @Scheduled(fixedDelay = REMINDER_INTERVAL)
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
                mailUtil.sendSimpleMessage(user.getEmail(), "noreply@traxter.com", subject, message)).start();
    }

    public void sendEmailConfirmationToken(User user, VerificationToken verToken)
            throws EmailAlreadyConfirmedException, InvalidRequestDataException {
        if (user.isEmailConfirmed()) {
            throw new EmailAlreadyConfirmedException();
        } else if (verToken.isPasswordReset()) {
            throw new InvalidRequestDataException(
                    "Verification token at confirmation is marked as password resetting token.");
        }
        String recipientAddress = user.getEmail();
        String token = verToken.getToken();
        String subject = "Confirm Email";
        String confirmationUrl = "http://localhost:8888/confirm?token=" + token;
        String message = "Please, click the following link to confirm your email:\n" + confirmationUrl;
        new Thread(() -> mailUtil.sendSimpleMessage(recipientAddress, "noreply@traxter.com", subject, message))
                .start();
    }
    public void sendPasswordResetLink(User user, VerificationToken verToken)
            throws UnauthorizedAccessException, InvalidRequestDataException {
        if (!user.isEmailConfirmed()) {
            throw new UnauthorizedAccessException("Email is not confirmed.");
        } else if (!verToken.isPasswordReset()) {
            throw new InvalidRequestDataException(
                    "Verification token at password reset is not marked as password resetting token.");
        }
        String recipientAddress = user.getEmail();
        String token = verToken.getToken();
        String subject = "Reset Password Key";
        String message = "Please, use the following key to reset your password:\n" + token;
        new Thread(() -> mailUtil.sendSimpleMessage(recipientAddress, "noreply@traxter.com", subject, message))
                .start();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST) // 400
    public static class EmailAlreadyConfirmedException extends MyException {
        private EmailAlreadyConfirmedException() {
            super("Email has already been confirmed.");
        }
    }

}
