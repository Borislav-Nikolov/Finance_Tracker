package finalproject.financetracker.model.dtos.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrationDTO {
    private String username;
    private String password;
    private String password2;
    private String firstName;
    private String lastName;
    private String email;
}