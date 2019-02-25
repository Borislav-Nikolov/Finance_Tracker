package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.ServerErrorException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< Updated upstream
import java.io.IOException;
import java.sql.SQLException;

=======
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
>>>>>>> Stashed changes
public abstract class AbstractController {

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }

    //--------------Exception Handlers---------------------//
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.EmailAlreadyUsedException.class})
    public String EmailAlreadyUsedException(UserController.EmailAlreadyUsedException ex) {
        return ex.getMessage();
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidPasswordAtRegistrationException.class})
    public void InvalidPasswordAtRegistrationException(UserController.InvalidPasswordAtRegistrationException ex,
                                                         HttpServletResponse response) throws IOException {
        response.sendRedirect("registration_errors/invalidPassword.html");
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


    //---------------------AccountController---------------------//
    //todo change msgs ---------------------------------/
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public String IOExceptionHandler(IOException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String SQLExceptionHandler(IOException e) {
        return e.getMessage();
    }

    @ExceptionHandler({MyException.class, JsonProcessingException.class})
    public String MyExceptionHandler(MyException e) throws MyException {
        throw e;
    }

    @ExceptionHandler(Exception.class)
    public String ExceptionHandler(Exception e) throws Exception {
        throw new ServerErrorException();
    }
}
