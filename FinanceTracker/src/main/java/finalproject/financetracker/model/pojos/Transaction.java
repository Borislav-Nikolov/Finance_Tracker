package finalproject.financetracker.model.pojos;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Transaction implements ITransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;

    @Column
    private String transactionName;

    @Column
    private double amount;

    @Column
    private Date executionDate;

    @Column
    private long userId;

    @Column
    private long categoryId;

    public Transaction(String transactionName, double amount, Date executionDate, long userId, long categoryId) {
        this.transactionName = transactionName;
        this.amount = amount;
        this.executionDate = executionDate;
        this.userId = userId;
        this.categoryId = categoryId;
    }
}
