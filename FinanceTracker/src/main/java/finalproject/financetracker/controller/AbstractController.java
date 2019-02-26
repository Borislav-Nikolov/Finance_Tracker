package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.exceptions.*;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.ErrMsg;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.exceptions.user_exceptions.*;
import javassist.tools.web.BadHttpRequest;
import netscape.security.ForbiddenTargetException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

@RestController
public abstract class AbstractController {

    //---------------------< Methods >----------------------//

    protected void validateLogin(HttpSession session) throws NotLoggedInException {
        if (!UserController.isLoggedIn(session)) {
            throw new NotLoggedInException();
        }
    }

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
    @ExceptionHandler({EmailAlreadyUsedException.class})
    public void EmailAlreadyUsedExceptionHandler(EmailAlreadyUsedException ex,
                                            HttpServletResponse response) throws IOException {

    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidPasswordAtRegistrationException.class})
    public void InvalidPasswordAtRegistrationExceptionHandler(InvalidPasswordAtRegistrationException ex,
                                                         HttpServletResponse response) throws IOException {
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidEmailException.class})
    public void InvalidEmailExceptionHandler(InvalidEmailException ex,
                                        HttpServletResponse response) throws IOException {
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserAlreadyExistsException.class})
    public void UserAlreadyExistsExceptionHandler(UserAlreadyExistsException ex,
                                             HttpServletResponse response) throws IOException {
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({RegistrationCheckException.class})
    public void RegistrationCheckExceptionHandler(RegistrationCheckException ex,
                                             HttpServletResponse response) throws IOException {
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidLoginInfoException.class})
    public void InvalidLoginInfoExceptionHandler(InvalidLoginInfoException ex,
                                           HttpServletResponse response) throws IOException {
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidPasswordInputException.class})
    public void InvalidPasswordInputExceptionHandler(InvalidPasswordInputException ex,
                                                 HttpServletResponse response) throws IOException {
    }


    //---------------------Global Exception Handlers---------------------//
    //todo change msgs ---------------------------------/

    @ExceptionHandler({
            MyException.class,
            JsonProcessingException.class,
            JsonParseException.class,
            JsonEOFException.class,
            HttpClientErrorException.BadRequest.class,
            BadHttpRequest.class,
            ServletException.class,
            HttpClientErrorException.class,
            HttpMessageNotReadableException.class})  //400
    public ErrMsg MyExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.BAD_REQUEST.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({
            NotLoggedInException.class,
            HttpClientErrorException.Unauthorized.class, })  // 401
    public ErrMsg MyLoginExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.UNAUTHORIZED.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({
            ForbiddenRequestException.class,
            HttpClientErrorException.Forbidden.class})  //403
    public ErrMsg MyForbiddenExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.FORBIDDEN.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({NotFoundException.class})  //404
    public ErrMsg MyNotFoundExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.NOT_FOUND.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler(IOException.class)  //500
    public ErrMsg IOExceptionHandler(Exception e) {
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({SQLException.class, DataAccessException.class}) //500
    public ErrMsg SQLExceptionHandler(Exception e) {
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler(Exception.class) //500
    public ErrMsg ExceptionHandler(Exception e){
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }
}
