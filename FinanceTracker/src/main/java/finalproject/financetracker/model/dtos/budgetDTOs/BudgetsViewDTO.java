package finalproject.financetracker.model.dtos.budgetDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BudgetsViewDTO {
    List<BudgetInfoDTO> budgets;
}
