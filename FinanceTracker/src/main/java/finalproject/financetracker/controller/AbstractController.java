package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.ErrMsg;
import finalproject.financetracker.model.Transaction;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import finalproject.financetracker.model.exceptions.ServerErrorException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

@RestController
public abstract class AbstractController {

    //---------------------< Methods >----------------------//

    protected boolean isValidAccount(Account a) {
        return a != null &&
                a.getAccountName() != null &&
                !a.getAccountName().isEmpty() &&
                !(a.getAmount() <= 0) &&
                a.getUserId() > 0;
    }

    protected boolean isNotValidTransaction(Transaction t) {
        return t == null ||
                t.getTransactionName() == null ||
                t.getTransactionName().isEmpty() ||
                (t.getAmount() <= 0) ||
                t.getUserId() <= 0 ||
                t.getCategoryId() <= 0 ||
                t.getAccountId() <= 0 ||
                t.getExecutionDate()==null;
    }

    protected User getLoggedUserWithIdFromSession(HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            InvalidRequestDataException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if(!UserController.isLoggedIn(sess)){
            throw new NotLoggedInException();
        }
        User u = mapper.readValue(sess.getAttribute("User").toString(), User.class);

        if (u == null || u.getUserId() <=0) {
            throw new InvalidRequestDataException();
        }
        return u;
    }

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }

    //---------------------< /Methods >----------------------//

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
    public ErrMsg IOExceptionHandler(IOException e) {
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public ErrMsg SQLExceptionHandler(IOException e) {
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({MyException.class, JsonProcessingException.class})
    public ErrMsg MyExceptionHandler(MyException e){
        return new ErrMsg(HttpStatus.BAD_REQUEST.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler(Exception.class)
    public ErrMsg ExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }
}
