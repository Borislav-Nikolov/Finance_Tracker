package finalproject.financetracker.exceptions.runntime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
public class ServerErrorException extends RuntimeException {

    public ServerErrorException(){
        super("Server error occurred");
    }

    public ServerErrorException(String msg){
        super(msg);
    }
}
