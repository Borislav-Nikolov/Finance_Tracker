package finalproject.financetracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PlannedTransaction extends Transaction implements IPlannedTransaction{
    @Column
    private long repeatPeriod;
}
