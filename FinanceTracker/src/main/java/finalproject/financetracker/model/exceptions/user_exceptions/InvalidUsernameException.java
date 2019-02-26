package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidUsernameException extends RegistrationValidationException {
    public InvalidUsernameException() {
        super("Invalid username input.");
    }
}
