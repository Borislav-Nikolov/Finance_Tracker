package finalproject.financetracker.model.exceptions.user_exceptions;

public class EmailAlreadyUsedException extends RegistrationValidationException {
    public EmailAlreadyUsedException() {
        super("That email is already taken.");
    }
}
