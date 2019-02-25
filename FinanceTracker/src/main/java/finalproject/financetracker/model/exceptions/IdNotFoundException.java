package finalproject.financetracker.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND) //404
public class IdNotFoundException extends MyException {

    public IdNotFoundException(){
        super("object with requested Id not found");
    }

    public IdNotFoundException(String msg){
        super(msg);
    }
}
