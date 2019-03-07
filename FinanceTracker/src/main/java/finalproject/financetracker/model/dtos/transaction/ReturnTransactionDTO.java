package finalproject.financetracker.model.dtos.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Long transactionId;
    private String transactionName;
    private Double amount;
    private LocalDateTime executionDate;
    private Long userId;
    private Long categoryId;
    private Long accountId;
    private String username;
    private String accountName;
    private String categoryName;
    private Boolean isIncome;

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
        if (u!=null) {
            this.username = u.getUsername();
        }
        return this;
    }

    public ReturnTransactionDTO withCategory(Category c) {
        if (c!=null) {
            this.categoryName = c.getCategoryName();
            this.isIncome = c.isIncome();
        }
        return this;
    }

    public ReturnTransactionDTO withAccount(ReturnAccountDTO a) {
        if (a!=null) {
            this.accountName = a.getAccountName();
        }
            return this;
    }

    public ReturnTransactionDTO withAccount(Account a) {
        if (a!=null) {
            this.accountName = a.getAccountName();
        }
        return this;
    }
}
