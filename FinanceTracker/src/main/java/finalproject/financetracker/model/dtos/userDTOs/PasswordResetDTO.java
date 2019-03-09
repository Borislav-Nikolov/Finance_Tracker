package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.dtos.IRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetDTO implements IRequestDTO {
    private String password;
    private String password2;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (password == null || password2 == null) {
            throw new InvalidRequestDataException("Null input given.");
        }
    }
}
