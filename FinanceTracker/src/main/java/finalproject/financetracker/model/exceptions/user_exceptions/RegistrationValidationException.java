package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class RegistrationValidationException extends MyException {
    public RegistrationValidationException(String msg) {
        super(msg);
    }
}
