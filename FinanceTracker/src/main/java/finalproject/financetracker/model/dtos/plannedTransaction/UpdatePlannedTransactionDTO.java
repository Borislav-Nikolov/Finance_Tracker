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
public class UpdatePlannedTransactionDTO implements IRequestDTO {
    private long transactionId;
    private String transactionName;
    private long repeatPeriod;

    @Override //TODO repeatPeriod >= 24*60*60*1000  (1 DAY)
    public void checkValid() throws InvalidRequestDataException {
        if (!(this.transactionId>0 &&
                this.transactionName != null &&
                !this.transactionName.isEmpty() &&
                this.repeatPeriod>0)){
            throw new InvalidRequestDataException();
        };
    }
}
