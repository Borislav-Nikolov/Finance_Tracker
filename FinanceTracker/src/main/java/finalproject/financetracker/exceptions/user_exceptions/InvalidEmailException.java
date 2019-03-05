package finalproject.financetracker.exceptions.user_exceptions;

public class InvalidEmailException extends RegistrationValidationException {
    public InvalidEmailException() {
        super("Invalid email input.");
    }
}
