package finalproject.financetracker.model.dtos.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddTransactionRespDTO {

    private long transactionId;

    private String transactionName;

    private double amount;

    private Date executionDate;

    private long userId;

    private long categoryId;

    private long accountId;

    private String username;

    private String acountName;

    private String categoryName;
}
