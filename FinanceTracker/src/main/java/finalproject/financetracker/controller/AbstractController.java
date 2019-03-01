package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.exceptions.*;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryException;
import finalproject.financetracker.model.exceptions.image_exceptions.ImageNotFoundException;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.ErrMsg;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.exceptions.user_exceptions.*;
import javassist.tools.web.BadHttpRequest;
import lombok.NoArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.jws.soap.SOAPBinding;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@NoArgsConstructor
@RestController
public abstract class AbstractController {

    //---------------------< Methods >----------------------//

    private Logger logger = LogManager.getLogger(Logger.class);

    private void logInfo(HttpStatus httpStatusCode, Exception e) {
        logger.info(httpStatusCode + "\n\tOccurred in class = " + this.getClass() + ",\n\tException class = " + e.getClass() + "\n\tmsg = " + e.getMessage());
    }

    private void logWarn(HttpStatus httpStatusCode, Exception e) {
        logger.warn(httpStatusCode + "\n\tOccurred in class = " + this.getClass() + ",\n\tException class = " + e.getClass() + "\n\tmsg = " + e.getMessage());
    }

    private void logError(HttpStatus httpStatusCode, Exception e) {
        logger.error(httpStatusCode + "\n\tOccurred in class = " + this.getClass() + ",\n\tException class = " + e.getClass() + "\n\tmsg = " + e.getMessage());
    }

    protected void validateLogin(HttpSession session) throws NotLoggedInException {
        if (!UserController.isLoggedIn(session)) {
            throw new NotLoggedInException();
        }
    }

    protected User getLoggedValidUserFromSession(HttpSession sess)
            throws
            NotLoggedInException,
            IOException{

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if(!UserController.isLoggedIn(sess)){
            throw new NotLoggedInException();
        }
        return mapper.readValue(sess.getAttribute("User").toString(), User.class);
    }

    protected void checkIfBelongsToLoggedUser(long resourceUserId, User u)
            throws
            NotLoggedInException{

        if (resourceUserId != u.getUserId() ) {
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
    }

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }


    //---------------------< /Methods >----------------------//

    //--------------Exception Handlers---------------------//
    // TODO transfer to below method
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            RegistrationValidationException.class,
            InvalidLoginInfoException.class,
            PasswordValidationException.class,
            RegistrationValidationException.class,
            CategoryException.class,
            ImageNotFoundException.class
    })
    public ErrMsg handleBadRequestExceptions(MyException ex) {
        return new ErrMsg(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), new Date());
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
        logError(HttpStatus.INTERNAL_SERVER_ERROR,e);
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({SQLException.class, DataAccessException.class}) //500
    public ErrMsg SQLExceptionHandler(Exception e) {
        logError(HttpStatus.INTERNAL_SERVER_ERROR,e);
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler(Exception.class) //500
    public ErrMsg ExceptionHandler(Exception e){
        logError(HttpStatus.INTERNAL_SERVER_ERROR,e);
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }
}
