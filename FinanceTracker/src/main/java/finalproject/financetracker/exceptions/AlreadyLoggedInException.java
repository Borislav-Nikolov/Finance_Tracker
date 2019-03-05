package finalproject.financetracker.exceptions;

public class AlreadyLoggedInException extends MyException {
    public AlreadyLoggedInException() {
        super("User already logged in.");
    }
}
