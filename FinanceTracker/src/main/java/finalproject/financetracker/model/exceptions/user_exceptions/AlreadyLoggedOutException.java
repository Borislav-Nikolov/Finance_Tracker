package finalproject.financetracker.model.exceptions.user_exceptions;

import finalproject.financetracker.model.exceptions.AlreadyLoggedInException;
import finalproject.financetracker.model.exceptions.MyException;

public class AlreadyLoggedOutException extends MyException {
    public AlreadyLoggedOutException() {
        super("User has already logged out.");
    }
}
