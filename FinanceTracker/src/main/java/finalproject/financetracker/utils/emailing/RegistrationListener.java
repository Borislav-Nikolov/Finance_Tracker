package finalproject.financetracker.utils.emailing;

import finalproject.financetracker.model.daos.TokenDao;
import finalproject.financetracker.model.repositories.TokenRepository;
import finalproject.financetracker.model.repositories.UserRepository;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.pojos.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private TokenDao tokenDao;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        this.confirmRegistration(onRegistrationCompleteEvent);
    }
    private void confirmRegistration(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        User user = onRegistrationCompleteEvent.getUser();
        VerificationToken verToken = tokenDao.getNewToken(user);
        String appUrl = onRegistrationCompleteEvent.getAppUrl();
        emailSender.sendEmailConfirmationToken(appUrl, verToken, user);
    }
}
