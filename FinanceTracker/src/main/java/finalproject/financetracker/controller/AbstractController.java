package finalproject.financetracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class AbstractController {
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.EmailAlreadyUsedException.class})
    public String EmailAlreadyUsedException(UserController.EmailAlreadyUsedException ex) {
        return ex.getMessage();
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidEmailException.class})
    public String InvalidEmailException(UserController.InvalidEmailException ex) {
        return ex.getMessage();
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.UserAlreadyExistsException.class})
    public String UserAlreadyExistsException(UserController.UserAlreadyExistsException ex) {
        return ex.getMessage();
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.RegistrationCheckException.class})
    public String RegistrationCheckException(UserController.RegistrationCheckException ex) {
        return ex.getMessage();
    }
}
