package finalproject.financetracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
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

    @Column
    private long accountId;

    @Transient
    private User user;

    @Transient
    private Category category;

    @Transient
    private Account account;
}
