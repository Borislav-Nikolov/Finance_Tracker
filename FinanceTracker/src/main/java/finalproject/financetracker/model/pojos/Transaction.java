package finalproject.financetracker.model.pojos;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity(name = "transactions")
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Transaction{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;

    @Column
    private String transactionName;

    @Column
    private double amount;

    @Column
    private LocalDateTime executionDate;

    @Column
    private long accountId;

    @Column
    private long categoryId;

    public Transaction(String transactionName, double amount, LocalDateTime executionDate,long accountId, long categoryId) {
        this.transactionName = transactionName;
        this.amount = amount;
        this.executionDate = executionDate;
        this.accountId = accountId;
        this.categoryId = categoryId;
    }
}
