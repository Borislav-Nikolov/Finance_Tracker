package finalproject.financetracker.model.dtos.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailChangeDTO {
    private String password;
    private String newEmail;
}
