package finalproject.financetracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PlannedTransaction extends Transaction implements IPlannedTransaction{

    @Column
    private long repeatPeriod;
}
