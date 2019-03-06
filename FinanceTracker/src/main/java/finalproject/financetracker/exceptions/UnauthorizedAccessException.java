package finalproject.financetracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED) // 401
public class UnauthorizedAccessException extends MyException {
    public UnauthorizedAccessException() {
        super("Unauthorized attempt.");
    }
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
