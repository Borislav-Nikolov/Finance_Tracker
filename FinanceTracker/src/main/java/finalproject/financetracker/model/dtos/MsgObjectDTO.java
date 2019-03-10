package finalproject.financetracker.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MsgObjectDTO {
    private String msg;
    private LocalDateTime time;
    Object object;
}
