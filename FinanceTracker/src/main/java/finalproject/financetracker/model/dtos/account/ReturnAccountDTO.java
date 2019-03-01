package finalproject.financetracker.model.dtos.account;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.IPlannedTransaction;
import finalproject.financetracker.model.pojos.ITransaction;
import finalproject.financetracker.model.pojos.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReturnAccountDTO implements IRequestDTO {
    private long accountId;
    private String accountName;
    private double amount;
    private long userId;
    private String username;
    private List<ITransaction> transactions;
    private List<IPlannedTransaction> plannedTransactions;

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

    public ReturnAccountDTO withTransactions(List<ITransaction> transactions){
        this.transactions = transactions;
        return this;
    }

    @Override
    public void checkValid() throws InvalidRequestDataException { }
}
