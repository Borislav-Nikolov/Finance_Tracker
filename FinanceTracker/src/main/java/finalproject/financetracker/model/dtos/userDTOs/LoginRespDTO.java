package finalproject.financetracker.model.dtos.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class LoginRespDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Date date;
}
