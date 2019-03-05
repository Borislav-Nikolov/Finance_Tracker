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

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (!(transactionId>0 &&
                transactionName != null &&
                !transactionName.isEmpty() &&
                this.repeatPeriod>0)){
            throw new InvalidRequestDataException();
        };
    }
}
