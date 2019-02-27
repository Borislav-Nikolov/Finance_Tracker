package finalproject.financetracker.model.exceptions.user_exceptions;

public class PasswordValidationException extends RegistrationValidationException {
    public PasswordValidationException(String msg) {
        super(msg);
    }
}
