package finalproject.financetracker.model.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MyException extends Exception {

    public MyException(String msg) {
        super(msg);
    }
}
