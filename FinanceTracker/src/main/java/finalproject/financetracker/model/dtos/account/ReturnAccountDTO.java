package finalproject.financetracker.model.dtos.account;

import finalproject.financetracker.model.dtos.IDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@AllArgsConstructor
public class ReturnAccountDTO implements IDTO {
    private long accountId;
    private String accountName;
    private double amount;
    private long userId;
    private String username;

    public ReturnAccountDTO(Account a){
        this.accountId = a.getAccountId();
        this.accountName = a.getAccountName().trim();
        this.amount = a.getAmount();
        this.userId = a.getUserId();
    }

    public ReturnAccountDTO withUsername(String username){
        this.username = username;
        return this;
    }

    @Override
    public void checkValid() throws InvalidRequestDataException { }
}
