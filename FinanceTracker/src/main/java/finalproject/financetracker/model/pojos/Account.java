package finalproject.financetracker.model.pojos;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Component
@Getter
@Setter
@Entity(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountId;
    private String accountName;
    private double amount;
    private long userId;

    public Account(String accountName, double amount, long userId) {
        this.accountName = accountName;
        this.amount = amount;
        this.userId = userId;
    }

}
