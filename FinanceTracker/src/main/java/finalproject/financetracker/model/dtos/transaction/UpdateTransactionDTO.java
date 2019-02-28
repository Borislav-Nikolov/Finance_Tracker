package finalproject.financetracker.model.dtos.transaction;

import finalproject.financetracker.model.dtos.IDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionDTO implements IDTO {

    private long transactionId;

    private String transactionName;

    private double amount;

    private long categoryId;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (!(transactionId > 0 &&
            transactionName != null &&
            !transactionName.isEmpty() &&
            amount > 0 &&
            categoryId > 0)){
            throw new InvalidRequestDataException();
        };
    }
}
