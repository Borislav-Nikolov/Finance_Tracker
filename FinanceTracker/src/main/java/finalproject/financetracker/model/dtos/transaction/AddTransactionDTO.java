package finalproject.financetracker.model.dtos.transaction;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddTransactionDTO implements IRequestDTO {

    private String transactionName;
    private Double amount;
    private Long categoryId;
    private Long accountId;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.transactionName == null || this.transactionName.isEmpty() ||
            this.amount== null || this.amount <= 0 ||
            this.categoryId == null || this.categoryId <=0||
            this.accountId == null || this.accountId <=0){
                throw new InvalidRequestDataException();
        }
    }
}
