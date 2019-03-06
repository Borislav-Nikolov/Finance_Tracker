package finalproject.financetracker.exceptions;

public class FailedActionException extends MyException {
    public FailedActionException() {
        super("Action failed.");
    }
    public FailedActionException(String message) {
        super(message);
    }
}
