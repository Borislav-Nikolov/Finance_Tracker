package finalproject.financetracker.model.dtos.budgetDTOs;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class BudgetCreationDTO implements IRequestDTO {
    private String budgetName;
    private Double amount;
    private String startingDate;
    private String endDate;
    private Long categoryId;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (budgetName == null ||
            amount == null ||
            startingDate == null ||
            endDate == null ||
            categoryId == null) {
            throw new InvalidRequestDataException("Improper data input at budget creation.");
        }
    }
}
