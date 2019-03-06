package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.pojos.VerificationToken;
import finalproject.financetracker.model.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenDao {

    @Autowired
    private TokenRepository tokenRepository;

    public VerificationToken getNewToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verToken = new VerificationToken(token, user.getUserId());
        tokenRepository.save(verToken);
        return verToken;
    }
}
