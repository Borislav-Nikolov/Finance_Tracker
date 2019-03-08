package finalproject.financetracker.model.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isEmailConfirmed;
    private boolean isSubscribed;
    private boolean isEligibleForPasswordReset;
    private Date lastNotified;
    private Date lastLogin;

    public User(String username, String password, String firstName, String lastName,
                String email, boolean isEmailConfirmed, boolean isSubscribed) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isEmailConfirmed = isEmailConfirmed;
        this.isSubscribed = isSubscribed;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
