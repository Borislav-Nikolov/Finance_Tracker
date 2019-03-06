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
    private double amount;
    private String startingDate;
    private String endDate;
    private long userId;
    private long categoryId;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (budgetName == null ||
            amount <= 0 ||
            startingDate == null ||
            endDate == null ||
            userId <= 0 ||
            categoryId <= 0) {
            throw new InvalidRequestDataException("Improper data input at budget creation.");
        }
    }
}
