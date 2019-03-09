package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.dtos.IRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileEditDTO implements IRequestDTO {
    private String password;
    private String newEmail;
    private String newPassword;
    private String newPassword2;
    private String firstName;
    private String lastName;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (password == null) {
            throw new InvalidRequestDataException("Null password input");
        }
    }
}
