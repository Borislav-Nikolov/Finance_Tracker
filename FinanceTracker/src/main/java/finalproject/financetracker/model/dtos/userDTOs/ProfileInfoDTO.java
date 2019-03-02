package finalproject.financetracker.model.dtos.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileInfoDTO {
    private long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isEmailConfirmed;
    private boolean isSubscribed;
}
