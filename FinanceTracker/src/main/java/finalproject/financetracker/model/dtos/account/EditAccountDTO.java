package finalproject.financetracker.model.dtos.account;

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
public class EditAccountDTO implements IRequestDTO {
    private long accountId;
    private String accountName;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if(getAccountName() == null ||
           getAccountName().isEmpty()){
            throw new InvalidRequestDataException();
        }
    }
}
