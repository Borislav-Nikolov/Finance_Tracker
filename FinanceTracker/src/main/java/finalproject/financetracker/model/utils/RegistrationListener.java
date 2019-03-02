package finalproject.financetracker.model.utils;

import finalproject.financetracker.model.daos.TokenRepository;
import finalproject.financetracker.model.daos.UserRepository;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.pojos.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    UserRepository userRepository;
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        this.confirmRegistration(onRegistrationCompleteEvent);
    }
    private void confirmRegistration(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        User user = onRegistrationCompleteEvent.getUser();
        String token = UUID.randomUUID().toString();
//        long userId = userRepository.findByUsername(user.getUsername()).getUserId();
        VerificationToken verToken = new VerificationToken(token, user.getUserId());
        tokenRepository.save(verToken);
        String recipientAddress = user.getEmail();
        String subject = "Confirm Email";
        String confirmationUrl = onRegistrationCompleteEvent.getAppUrl() + "/confirm?token=" + token;
        String message = "Please, click the following link to confirm your email:\n" + confirmationUrl;
        mailUtil.sendSimpleMessage(recipientAddress, "noreply@traxter.com", subject, message);
    }
}
