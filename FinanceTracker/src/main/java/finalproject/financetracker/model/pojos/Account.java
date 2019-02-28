package finalproject.financetracker.model.pojos;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class Account {
    private long accountId;
    private String accountName;
    private double amount;
    private long userId;

    public Account(String accountName, double amount, long userId) {
        this.accountName = accountName;
        this.amount = amount;
        this.userId = userId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getAmount() {
        return amount;
    }

    public long getUserId() {
        return userId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", accountName='" + accountName + '\'' +
                ", amount=" + amount +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Double.compare(account.amount, amount) == 0 &&
                userId == account.userId &&
                Objects.equals(accountName, account.accountName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountName, amount, userId);
    }
}
