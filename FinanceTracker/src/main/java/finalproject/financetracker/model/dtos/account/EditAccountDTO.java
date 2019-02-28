package finalproject.financetracker.model.dtos.account;

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
public class EditAccountDTO implements IDTO {
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
