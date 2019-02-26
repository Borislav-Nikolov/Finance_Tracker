package finalproject.financetracker.model.exceptions.user_exceptions;

public class InvalidEmailException extends RegistrationCheckException{
    public InvalidEmailException() {
        super("Invalid email input.");
    }
}
