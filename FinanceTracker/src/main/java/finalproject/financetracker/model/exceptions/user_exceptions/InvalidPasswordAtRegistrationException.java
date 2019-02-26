package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidPasswordAtRegistrationException extends RegistrationCheckException {
    public InvalidPasswordAtRegistrationException() {
        super("Password must be at least 3 symbols."); // TODO make this validation
    }
}
