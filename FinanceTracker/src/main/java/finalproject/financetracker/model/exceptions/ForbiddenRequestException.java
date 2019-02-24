package finalproject.financetracker.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenRequestException extends MyException {
    public ForbiddenRequestException(){
        super("forbidden request");
    }
    public ForbiddenRequestException(String msg){
        super(msg);
    }
}
