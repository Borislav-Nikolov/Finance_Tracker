package finalproject.financetracker.model.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN)  // 403
public class NotLoggedInException extends MyException {

    public NotLoggedInException(){
        super("not logged in");
    }

    public NotLoggedInException(String msg){
        super(msg);
    }
}
