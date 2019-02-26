package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class RegistrationCheckException extends MyException {
    public RegistrationCheckException(String msg) {
        super(msg);
    }
}
