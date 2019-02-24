package finalproject.financetracker.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SQLCreateSchemaException extends MyException{
    public SQLCreateSchemaException(){
        super("Error creating DB scheme");
    }

    public SQLCreateSchemaException(String msg){
        super(msg);
    }
}
