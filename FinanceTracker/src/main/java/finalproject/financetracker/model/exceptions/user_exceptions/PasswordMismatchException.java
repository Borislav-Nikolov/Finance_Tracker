package finalproject.financetracker.model.exceptions.user_exceptions;

public class PasswordMismatchException extends RegistrationValidationException {
    public PasswordMismatchException() {
        super("Passwords do not match.");
    }
}
