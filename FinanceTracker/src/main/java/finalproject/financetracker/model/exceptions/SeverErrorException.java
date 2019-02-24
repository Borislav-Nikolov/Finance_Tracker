package finalproject.financetracker.model.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SeverErrorException extends Exception {

    public SeverErrorException(){
        super("Server error occurred");
    }

    public SeverErrorException(String msg){
        super(msg);
    }
}
