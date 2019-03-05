package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginInfoDTO implements IRequestDTO {
    private String username;
    private String password;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.username == null || this.password == null) {
            throw new InvalidRequestDataException("Null values given as login info.");
        }
    }
}
