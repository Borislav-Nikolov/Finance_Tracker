package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidPasswordAtRegistrationException extends RegistrationValidationException {
    public InvalidPasswordAtRegistrationException() {
        super("Password must be at least 3 symbols.");
    }
}
