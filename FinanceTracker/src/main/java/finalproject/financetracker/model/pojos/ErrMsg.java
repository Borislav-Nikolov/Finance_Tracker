package finalproject.financetracker.model.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrMsg {
    private int status;
    private String msg;
    private LocalDateTime time;
}
