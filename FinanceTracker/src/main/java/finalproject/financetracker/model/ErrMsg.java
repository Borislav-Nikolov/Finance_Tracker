package finalproject.financetracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ErrMsg {

    private int status;
    private String msg;
    private Date Date;
}
