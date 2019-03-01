package finalproject.financetracker.model.dtos.transaction;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionDTO implements IRequestDTO {

    private long transactionId;
    private String transactionName;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (!(transactionId>0 &&
            transactionName != null &&
            !transactionName.isEmpty())){
            throw new InvalidRequestDataException();
        };
    }
}
