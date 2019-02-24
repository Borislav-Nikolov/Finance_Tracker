package finalproject.financetracker.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST/*, reason="invalid request data"*/)  // 400
public class InvalidRequestDataException extends MyException{

    public InvalidRequestDataException(){
        super("invalid request data");
    }

    public InvalidRequestDataException(String msg){
        super(msg);
    }
}
