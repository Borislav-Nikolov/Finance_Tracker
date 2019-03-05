package finalproject.financetracker.exceptions.user_exceptions;

import finalproject.financetracker.exceptions.MyException;

public class RegistrationValidationException extends MyException {
    public RegistrationValidationException(String msg) {
        super(msg);
    }
}
