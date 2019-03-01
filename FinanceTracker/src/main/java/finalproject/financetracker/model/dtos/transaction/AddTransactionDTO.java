package finalproject.financetracker.model.dtos.transaction;

import com.sun.mail.imap.protocol.ID;
import finalproject.financetracker.model.dtos.IDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddTransactionDTO implements IDTO {

    private String transactionName;

    private double amount;

    private long categoryId;


    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (this.transactionName == null ||
            this.transactionName.isEmpty() ||
            this.amount <= 0 ||
            this.categoryId<=0){
                throw new InvalidRequestDataException();
        }
    }
}
