package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidPasswordInputException extends PasswordValidationException {
    public InvalidPasswordInputException() {
        super("Wrong password.");
    }
}
