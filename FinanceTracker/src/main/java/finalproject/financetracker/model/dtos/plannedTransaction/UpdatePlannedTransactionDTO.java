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
public class UpdatePlannedTransactionDTO implements IRequestDTO {
    private long transactionId;
    private String transactionName;
    private long repeatPeriod;
    public static final int SECOND_IN_DAY = 86400000;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (!(this.transactionId > 0 &&
                this.transactionName != null &&
                !this.transactionName.isEmpty() &&
                this.repeatPeriod >= SECOND_IN_DAY)) {
            throw new InvalidRequestDataException();
        }
        ;
    }
}
