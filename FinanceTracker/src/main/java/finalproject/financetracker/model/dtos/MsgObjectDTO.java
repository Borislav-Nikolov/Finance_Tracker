package finalproject.financetracker.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class MsgObjectDTO {
    private String msg;
    private Date date;
    Object object;
}
