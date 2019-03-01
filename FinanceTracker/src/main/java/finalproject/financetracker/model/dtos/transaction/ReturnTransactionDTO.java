package finalproject.financetracker.model.dtos.transaction;

import finalproject.financetracker.model.dtos.IDTO;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.pojos.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionDTO implements IDTO {

    private long transactionId;
    private String transactionName;
    private double amount;
    private Date executionDate;
    private long userId;
    private long categoryId;
    private long accountId;
    private String username;
    private String accountName;
    private String categoryName;
    private boolean isIncome;

    public ReturnTransactionDTO(Transaction t){
        this.transactionId = t.getTransactionId();
        this.transactionName = t.getTransactionName().trim();
        this.amount = t.getAmount();
        this.userId = t.getUserId();
        this.categoryId = t.getCategoryId();
    }

    public ReturnTransactionDTO withUsername(String username){
        this.username = username;
        return this;
    }

    public ReturnTransactionDTO withCategoryName(String categoryName){
        this.categoryName = categoryName;
        return this;
    }

    @Override
    public void checkValid() {
    }
}
