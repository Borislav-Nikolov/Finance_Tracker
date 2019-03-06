package finalproject.financetracker.utils.passCrypt;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PassCrypter {

    private static final int LOG_ROUNDS = 13;

    public String crypt(String pass){
        return BCrypt.hashpw(pass, BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean check(String pass, String hashedPass){
        return BCrypt.checkpw(pass, hashedPass);
    }
}
