package finalproject.financetracker.exceptions.user_exceptions;

public class InvalidPasswordException extends PasswordValidationException {
    public InvalidPasswordException(String message) {
        super(message);
    }
    public InvalidPasswordException() {
        super("Password must be at least 3 symbols.");
    }
}
