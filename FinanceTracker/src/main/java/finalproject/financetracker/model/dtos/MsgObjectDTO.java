package finalproject.financetracker.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MsgObjectDTO {
    CommonMsgDTO message;
    Object object;
}
