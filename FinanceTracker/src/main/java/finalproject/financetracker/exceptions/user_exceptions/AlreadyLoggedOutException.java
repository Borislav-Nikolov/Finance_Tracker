package finalproject.financetracker.exceptions.user_exceptions;

import finalproject.financetracker.exceptions.MyException;

public class AlreadyLoggedOutException extends MyException {
    public AlreadyLoggedOutException() {
        super("User has already logged out.");
    }
}
