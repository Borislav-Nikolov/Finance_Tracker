package finalproject.financetracker.exceptions.user_exceptions;

public class UserAlreadyExistsException extends RegistrationValidationException {
    public UserAlreadyExistsException() {
        super("That username is already taken.");
    }
}
