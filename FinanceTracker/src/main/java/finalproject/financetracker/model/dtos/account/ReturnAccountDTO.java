package finalproject.financetracker.model.dtos.account;

import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReturnAccountDTO{
    private long accountId;
    private String accountName;
    private double amount;
    private long userId;
    private String username;
    private List<ReturnTransactionDTO> transactions;
    private List<ReturnPlannedTransactionDTO> plannedTransactions;

    public ReturnAccountDTO(Account a){
        this.accountId = a.getAccountId();
        this.accountName = a.getAccountName().trim();
        this.amount = a.getAmount();
        this.userId = a.getUserId();
    }

    public ReturnAccountDTO withUser(User u){
        this.username = u.getUsername();
        return this;
    }

    public ReturnAccountDTO withTransactions(List<ReturnTransactionDTO> transactions){
        this.transactions = transactions;
        return this;
    }

    public ReturnAccountDTO withPlannedTransactions(List<ReturnPlannedTransactionDTO> plannedTransactions){
        this.plannedTransactions = plannedTransactions;
        return this;
    }
}
