package finalproject.financetracker.model.dtos.budgetDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class BudgetCreationDTO {
    private String budgetName;
    private double amount;
    private LocalDate startingDate;
    private LocalDate endDate;
    private long userId;
    private long categoryId;
}
