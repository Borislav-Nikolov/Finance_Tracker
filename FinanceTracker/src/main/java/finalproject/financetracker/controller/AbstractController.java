package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.ServerErrorException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

@RestController
public abstract class AbstractController {

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }

    //--------------Exception Handlers---------------------//
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.EmailAlreadyUsedException.class})
    public void EmailAlreadyUsedExceptionHandler(UserController.EmailAlreadyUsedException ex,
                                            HttpServletResponse response) throws IOException {
        response.sendRedirect("registration_errors/usedEmail.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidPasswordAtRegistrationException.class})
    public void InvalidPasswordAtRegistrationExceptionHandler(UserController.InvalidPasswordAtRegistrationException ex,
                                                         HttpServletResponse response) throws IOException {
        response.sendRedirect("registration_errors/invalidPassword.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidEmailException.class})
    public void InvalidEmailExceptionHandler(UserController.InvalidEmailException ex,
                                        HttpServletResponse response) throws IOException {
        response.sendRedirect("registration_errors/invalidEmail.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.UserAlreadyExistsException.class})
    public void UserAlreadyExistsExceptionHandler(UserController.UserAlreadyExistsException ex,
                                             HttpServletResponse response) throws IOException {
        response.sendRedirect("registration_errors/userExists.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.RegistrationCheckException.class})
    public void RegistrationCheckExceptionHandler(UserController.RegistrationCheckException ex,
                                             HttpServletResponse response) throws IOException {
        response.sendRedirect("/register.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidLoginInfoException.class})
    public void InvalidLoginInfoExceptionHandler(UserController.InvalidLoginInfoException ex,
                                           HttpServletResponse response) throws IOException {
        response.sendRedirect("error_pages/invalidLoginInfo.html");
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserController.InvalidPasswordInputException.class})
    public void InvalidPasswordInputExceptionHandler(UserController.InvalidPasswordInputException ex,
                                                 HttpServletResponse response) throws IOException {
        response.sendRedirect("error_pages/invalidPasswordInput.html");
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
