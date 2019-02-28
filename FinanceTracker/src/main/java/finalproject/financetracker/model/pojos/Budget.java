package finalproject.financetracker.model.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long budgetId;
    private String budgetName;
    private double amount;
    private LocalDate startingDate;
    private LocalDate endDate;
    private long userId;
    private long categoryId;

    public Budget(String budgetName, double amount, LocalDate startingDate, LocalDate endDate, long userId, long categoryId) {
        this.budgetName = budgetName;
        this.amount = amount;
        this.startingDate = startingDate;
        this.endDate = endDate;
        this.userId = userId;
        this.categoryId = categoryId;
    }
}
