package finalproject.financetracker.model.dtos.budgetDTOs;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BudgetInfoDTO {
    private long budgetId;
    private String budgetName;
    private double amount;
    private LocalDate startingDate;
    private LocalDate endDate;
    private long userId;
    private long categoryId;
}
