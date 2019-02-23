package finalproject.financetracker.model.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN, reason="not logged in")  // 403
public class NotLoggedInException extends Exception {

    public NotLoggedInException(){
        super("not logged in");
    }
}
