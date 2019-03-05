package finalproject.financetracker.exceptions.user_exceptions;

import finalproject.financetracker.exceptions.MyException;

public class InvalidLoginInfoException extends MyException {
    public InvalidLoginInfoException() {
        super("Wrong user or password.");
    }
}
