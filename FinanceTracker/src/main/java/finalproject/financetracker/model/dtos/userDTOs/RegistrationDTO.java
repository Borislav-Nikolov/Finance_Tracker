package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrationDTO implements IRequestDTO {
    private String username;
    private String password;
    private String password2;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isSubscribed;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.username == null ||
            this.password == null ||
            this.password2 == null ||
            this.firstName == null ||
            this.lastName == null ||
            this.email == null) {
            throw new InvalidRequestDataException("Null request data input at user registration.");
        }
    }
}