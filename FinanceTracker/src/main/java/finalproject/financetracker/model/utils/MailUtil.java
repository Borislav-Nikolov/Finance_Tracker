package finalproject.financetracker.model.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {
    @Autowired
    JavaMailSender javaMailSender;
    public void sendSimpleMessage(String to, String from, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        // TODO https://stackoverflow.com/questions/13946581/spring-java-mail-the-from-address-is-being-ignored
        // message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
}

