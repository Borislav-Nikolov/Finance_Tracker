package finalproject.financetracker.model.dtos.userDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PassChangeDTO {
    private String oldPass;
    private String newPass;
    private String newPass2;
}
