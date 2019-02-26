package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class InvalidPasswordInputException extends RegistrationCheckException {
    public InvalidPasswordInputException() {
        super("Passwords must match and not be empty.");
    }
}
