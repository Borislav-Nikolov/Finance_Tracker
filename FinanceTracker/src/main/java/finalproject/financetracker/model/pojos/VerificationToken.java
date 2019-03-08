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
    private static final int EMAIL_VERIFICATION_EXPIRATION = 60 * 24;
    public static final int PASSWORD_RESET_EXPIRATION = 15;

    @Id
    private Long userId;

    private String token;

    private Date expiryDate;

    private boolean isPasswordReset;

    public VerificationToken(String token, long userId, boolean isPasswordReset) {
        this.token = token;
        this.userId = userId;
        if (!isPasswordReset) {
            this.expiryDate = this.calculateEmailVerificationExpiryDate();
        } else {
            // TODO finish later (set different with new method)
        }
        this.isPasswordReset = isPasswordReset;
    }

    private Date calculateEmailVerificationExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, EMAIL_VERIFICATION_EXPIRATION);
        return new Date(cal.getTime().getTime());
    }
}
