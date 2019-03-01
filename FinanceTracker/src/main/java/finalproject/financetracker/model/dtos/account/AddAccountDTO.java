package finalproject.financetracker.model.dtos.account;

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
public class AddAccountDTO implements IRequestDTO {
    private String accountName;
    private double amount;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if(getAccountName() == null ||
           getAccountName().isEmpty() ||
           getAmount() <= 0){
            throw new InvalidRequestDataException();
        }
    }
}
