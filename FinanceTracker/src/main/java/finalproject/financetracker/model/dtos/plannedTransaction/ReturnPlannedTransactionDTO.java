package finalproject.financetracker.model.dtos.plannedTransaction;

import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnPlannedTransactionDTO {
    private long transactionId;
    private String transactionName;
    private double amount;
    private LocalDateTime nextExecutionDate;
    private long repeatPeriod;
    private long userId;
    private long categoryId;
    private long accountId;
    private String username;
    private String accountName;
    private String categoryName;
    private Boolean isIncome;

    public ReturnPlannedTransactionDTO(PlannedTransaction t) {
        this.transactionId = t.getPtId();
        this.transactionName = t.getPtName().trim();
        this.nextExecutionDate = t.getNextExecutionDate();
        this.repeatPeriod = t.getRepeatPeriod();
        this.amount = t.getPtAmount();
        this.userId = t.getUserId();
        this.categoryId = t.getCategoryId();
        this.accountId = t.getAccountId();
    }

    public ReturnPlannedTransactionDTO withUser(User u) {
        this.username = u.getUsername();
        return this;
    }

    public ReturnPlannedTransactionDTO withCategory(Category category) {
        this.categoryName = category.getCategoryName();
        this.isIncome = category.isIncome();
        return this;
    }

    public ReturnPlannedTransactionDTO withAccount(ReturnAccountDTO a) {
        this.accountName = a.getAccountName();
        return this;
    }

    public ReturnPlannedTransactionDTO withAccount(Account a) {
        this.accountName = a.getAccountName();
        return this;
    }
}
