package finalproject.financetracker.model.dtos.plannedTransaction;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.dtos.IRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddPlannedTransactionDTO implements IRequestDTO {
    private String transactionName;
    private long executionOffset;
    private double amount;
    private long categoryId;
    private long accountId;
    private long repeatPeriod;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.transactionName == null ||
                this.transactionName.isEmpty() ||
                this.executionOffset < 0 ||
                this.amount <= 0 ||
                this.categoryId <= 0 ||
                this.accountId <= 0 ||
                this.repeatPeriod == 0) {  //TODO repeat period >= 1 day in millis
            throw new InvalidRequestDataException();
        }
    }
}
