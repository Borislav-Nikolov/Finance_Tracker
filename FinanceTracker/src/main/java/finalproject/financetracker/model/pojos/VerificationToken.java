package finalproject.financetracker.model.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    private static final int EMAIL_VERIFICATION_EXPIRATION_MIN = 60 * 24; // 24 hours
    public static final int PASSWORD_RESET_EXPIRATION_MIN = 15; // 15 minutes

    @Id
    private Long userId;

    private String token;

    private Date expiryDate;

    private boolean isPasswordReset;

    public VerificationToken(String token, long userId, boolean isPasswordReset) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = this.calculateEmailVerificationExpiryDate(isPasswordReset);
        this.isPasswordReset = isPasswordReset;
    }

    private Date calculateEmailVerificationExpiryDate(boolean isPasswordReset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        if (isPasswordReset) {
            cal.add(Calendar.MINUTE, PASSWORD_RESET_EXPIRATION_MIN);
        } else {
            cal.add(Calendar.MINUTE, EMAIL_VERIFICATION_EXPIRATION_MIN);
        }
        return new Date(cal.getTime().getTime());
    }
}
