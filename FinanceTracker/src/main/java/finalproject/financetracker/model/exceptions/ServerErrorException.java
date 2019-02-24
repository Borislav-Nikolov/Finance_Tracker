package finalproject.financetracker.model.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerErrorException extends Exception {

    public ServerErrorException(){
        super("Server error occurred");
    }

    public ServerErrorException(String msg){
        super(msg);
    }
}
