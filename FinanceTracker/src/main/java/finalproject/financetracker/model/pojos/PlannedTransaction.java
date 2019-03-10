package finalproject.financetracker.model.pojos;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "planned_transactions")
public class PlannedTransaction{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ptId;

    @Column
    private String ptName;

    @Column
    private double ptAmount;

    @Column
    private LocalDateTime nextExecutionDate;

    @Column
    private long accountId;

    @Column
    private long categoryId;

    @Column
    private long repeatPeriod;

    public PlannedTransaction(String ptName, double ptAmount, LocalDateTime nextExecutionDate, long accountId, long categoryId, long repeatPeriod) {
        this.ptName = ptName;
        this.ptAmount = ptAmount;
        this.nextExecutionDate = nextExecutionDate;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.repeatPeriod = repeatPeriod;
    }
}
