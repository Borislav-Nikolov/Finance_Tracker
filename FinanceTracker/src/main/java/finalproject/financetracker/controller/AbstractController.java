package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.ErrMsg;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.utils.emailing.EmailSender;
import lombok.NoArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

@NoArgsConstructor
@RestController
public abstract class AbstractController {
    public static final int SEC_TO_MILLIS = 1000;
    static final String SESSION_USERNAME_KEY = "Username";
    static final String SESSION_USER_KEY = "User";
    static final String SESSION_IP_ADDR_KEY = "IpAddr";

    //---------------------< Methods >----------------------//

    private Logger logger = LogManager.getLogger(Logger.class);

    protected void logInfo(String msg) {
        logger.info(msg);
    }

    protected void logInfo(HttpStatus httpStatusCode, Exception e) {
        logger.info(httpStatusCode
                + "\n\tOccurred in class = " + this.getClass()
                + ",\n\tException class = " + e.getClass()
                + "\n\tmsg = " + e.getMessage());
    }

    protected void logWarn(HttpStatus httpStatusCode, Exception e) {
        logger.warn(httpStatusCode
                + "\n\tOccurred in class = " + this.getClass()
                + ",\n\tException class = " + e.getClass()
                + "\n\tmsg = " + e.getMessage(),e);
    }

    protected void logError(HttpStatus httpStatusCode, Exception e) {
        logger.error(httpStatusCode
                + "\n\tOccurred in class = " + this.getClass()
                + ",\n\tException class = " + e.getClass()
                + "\n\tmsg = " + e.getMessage(),e);
    }

    protected User getLoggedValidUserFromSession(HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            MyException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if(!UserController.isLoggedIn(sess)){
            throw new NotLoggedInException();
        }
        this.validateIpAddr(sess, request);
        return mapper.readValue(sess.getAttribute(SESSION_USER_KEY).toString(), User.class);
    }

    protected void checkIfBelongsToLoggedUser(long resourceUserId, User u)
            throws
            ForbiddenRequestException{

        if (resourceUserId != u.getUserId() ) {
            throw new ForbiddenRequestException();
        }
    }

    protected <T extends Account> void checkIfAccountBelongsToLoggedUser(Long resourceUserId, JpaRepository<T, Long> repo, Class<T> c, User u)
            throws
            MyException {
        T t = validateDataAndGetByIdFromRepo(resourceUserId,repo,c);
        if (t.getUserId() != u.getUserId() ) {
            throw new ForbiddenRequestException();
        }
    }

    protected User checkIfBelongsToLoggedUserAndReturnUser(
            long resourceUserId,
            HttpSession session,
            HttpServletRequest request)

            throws
            MyException,
            IOException{

        User u = getLoggedValidUserFromSession(session, request);
        checkIfBelongsToLoggedUser(resourceUserId,u);
        return u;
    }

    protected <T extends Object> void checkIfNotNull(Class<?> c,T t ) throws NotFoundException {
        String className = c.getName().substring(c.getName().lastIndexOf(".")+1);
        if (t == null) throw new NotFoundException(className + " not found");
    }

    protected <T> T checkIfOptionalPresent(Class<?> c, Optional<T> o ) throws NotFoundException {
        String className = c.getName().substring(c.getName().lastIndexOf(".")+1);
        if (!o.isPresent())throw new NotFoundException(className + " not found");
        return o.get();
    }

    protected <T> T validateDataAndGetByIdFromRepo(String id,
                                                   JpaRepository<T,Long> repo,
                                                   Class<?>c)
            throws NotFoundException,
            InvalidRequestDataException {

        long idL = parseLong(id);
        Optional<T> t = repo.findById(idL);
        return checkIfOptionalPresent(c,t);
    }

    protected <T> T validateDataAndGetByIdFromRepo(long id,
                                                   JpaRepository<T,Long> repo,
                                                   Class<?>c)
            throws NotFoundException {

        Optional<T> t = repo.findById(id);
        return checkIfOptionalPresent(c,t);
    }

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }

    public Integer parseInt(String num) throws InvalidRequestDataException {
        try {
            return Integer.parseInt(num);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestDataException("Non-numeric value given.");
        }
    }

    public Long parseLong(String num) throws InvalidRequestDataException {
        try {
            return Long.parseLong(num);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestDataException("Non-numeric value given.");
        }
    }

    public Double parseDouble(String num) throws InvalidRequestDataException {
        try {
            return Double.parseDouble(num);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestDataException("Non-numeric value given.");
        }
    }

    void validateIpAddr(HttpSession session, HttpServletRequest request) throws UnauthorizedAccessException {
        if (!session.getAttribute(SESSION_IP_ADDR_KEY)
                .equals(request.getRemoteAddr())) {
            session.invalidate();
            throw new UnauthorizedAccessException("Attempt to access from another IP.");
        }
    }


    //---------------------< /Methods >----------------------//

    //---------------------Global Exception Handlers---------------------//
    //todo change msgs ---------------------------------/

    @ExceptionHandler({
            MyException.class,
            JsonProcessingException.class,
            JsonParseException.class,
            JsonEOFException.class,
            HttpMessageNotReadableException.class})  //400
    public ErrMsg MyExceptionHandler(Exception e, HttpServletResponse resp){
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ErrMsg(HttpStatus.BAD_REQUEST.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({
            NotLoggedInException.class,
            HttpClientErrorException.Unauthorized.class,
            UnauthorizedAccessException.class})  // 401
    public ErrMsg MyLoginExceptionHandler(Exception e, HttpServletResponse resp){
        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
        return new ErrMsg(HttpStatus.UNAUTHORIZED.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({ForbiddenRequestException.class})  //403
    public ErrMsg MyForbiddenExceptionHandler(Exception e, HttpServletResponse resp){
        resp.setStatus(HttpStatus.FORBIDDEN.value());
        return new ErrMsg(HttpStatus.FORBIDDEN.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({NotFoundException.class})  //404
    public ErrMsg MyNotFoundExceptionHandler(Exception e, HttpServletResponse resp){
        resp.setStatus(HttpStatus.NOT_FOUND.value());
        return new ErrMsg(HttpStatus.NOT_FOUND.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({FailedActionException.class}) // 417
    public ErrMsg expectationFailedHandler(Exception e, HttpServletResponse resp) {
        resp.setStatus(HttpStatus.EXPECTATION_FAILED.value());
        return new ErrMsg(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage(),new Date());
    }

    @ExceptionHandler({Exception.class,SQLException.class, DataAccessException.class,IOException.class}) //500
    public ErrMsg ExceptionHandler(Exception e, HttpServletResponse resp){
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        logError(HttpStatus.INTERNAL_SERVER_ERROR,e);
        return new ErrMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(),new Date());
    }
}
