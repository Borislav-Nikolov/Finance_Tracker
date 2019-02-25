package finalproject.financetracker.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) //404
public class NotFoundException extends MyException{
    public NotFoundException(){
        super("not found");
    }

    public NotFoundException(String msg){
        super(msg);
    }
}
