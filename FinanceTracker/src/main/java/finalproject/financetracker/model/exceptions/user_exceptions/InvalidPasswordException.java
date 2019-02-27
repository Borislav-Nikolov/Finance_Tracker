package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidPasswordException extends PasswordValidationException {
    public InvalidPasswordException() {
        super("Password must be at least 3 symbols.");
    }
}
