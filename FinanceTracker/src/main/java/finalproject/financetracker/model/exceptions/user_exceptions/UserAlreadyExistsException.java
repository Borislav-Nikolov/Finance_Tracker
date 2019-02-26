package finalproject.financetracker.model.exceptions.user_exceptions;

public class UserAlreadyExistsException extends RegistrationCheckException {
    public UserAlreadyExistsException() {
        super("That username is already taken.");
    }
}
