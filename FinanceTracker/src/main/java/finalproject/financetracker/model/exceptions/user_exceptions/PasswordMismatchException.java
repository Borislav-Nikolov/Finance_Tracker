package finalproject.financetracker.model.exceptions.user_exceptions;

public class PasswordMismatchException extends PasswordValidationException {
    public PasswordMismatchException() {
        super("Passwords do not match.");
    }
}
