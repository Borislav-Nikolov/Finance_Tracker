package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.dtos.IRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailChangeDTO implements IRequestDTO {
    private String password;
    private String newEmail;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.password == null ||
            this.newEmail == null) {
            throw new InvalidRequestDataException("Null values given at email change.");
        }
    }
}
