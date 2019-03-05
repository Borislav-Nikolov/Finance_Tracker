package finalproject.financetracker.model.dtos.transaction;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionDTO {

    private long transactionId;
    private String transactionName;
    private double amount;
    private LocalDateTime executionDate;
    private long userId;
    private long categoryId;
    private long accountId;
    private String username;
    private String accountName;
    private String categoryName;
    private boolean isIncome;

    public ReturnTransactionDTO(Transaction t) {
        this.transactionId = t.getTransactionId();
        this.transactionName = t.getTransactionName().trim();
        this.executionDate = t.getExecutionDate();
        this.amount = t.getAmount();
        this.userId = t.getUserId();
        this.categoryId = t.getCategoryId();
        this.accountId = t.getAccountId();
    }

    public ReturnTransactionDTO withUser(User u) {
        this.username = u.getUsername();
        return this;
    }

    public ReturnTransactionDTO withCategory(Category c) {
        this.categoryName = c.getCategoryName();
        this.isIncome = c.isIncome();
        return this;
    }

    public ReturnTransactionDTO withAccount(ReturnAccountDTO a) {
        this.accountName = a.getAccountName();
        return this;
    }

    public ReturnTransactionDTO withAccount(Account a) {
        this.accountName = a.getAccountName();
        return this;
    }
}
