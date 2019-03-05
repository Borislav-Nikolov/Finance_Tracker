package finalproject.financetracker.model.dtos.userDTOs;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.dtos.IRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PassChangeDTO implements IRequestDTO {
    private String oldPass;
    private String newPass;
    private String newPass2;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.oldPass == null ||
            this.newPass == null ||
            this.newPass2 == null) {
            throw new InvalidRequestDataException("Null parameters given at password change.");
        }
    }
}
