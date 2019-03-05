package finalproject.financetracker.model.dtos.plannedTransaction;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
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
    private double amount;
    private long categoryId;
    private long accountId;
    private long repeatPeriod;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.transactionName == null ||
                this.transactionName.isEmpty() ||
                this.amount <= 0 ||
                this.categoryId<=0||
                this.accountId<=0 ||
                this.repeatPeriod == 0){
            throw new InvalidRequestDataException();
        }
    }
}
