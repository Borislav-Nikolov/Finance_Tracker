package finalproject.financetracker.exceptions;

public class BadRequestException extends MyException {
    public BadRequestException() {
        super("Action not possible.");
    }
    public BadRequestException(String message) {
        super(message);
    }
}
