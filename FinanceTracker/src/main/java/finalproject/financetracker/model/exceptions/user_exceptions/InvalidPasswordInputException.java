package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class InvalidPasswordInputException extends MyException {
    public InvalidPasswordInputException() {
        super("Wrong password.");
    }
}
