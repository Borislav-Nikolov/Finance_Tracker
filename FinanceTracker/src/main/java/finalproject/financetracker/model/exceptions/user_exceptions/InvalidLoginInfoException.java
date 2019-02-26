package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class InvalidLoginInfoException extends MyException {
    public InvalidLoginInfoException() {
        super("Wrong user or password.");
    }
}
