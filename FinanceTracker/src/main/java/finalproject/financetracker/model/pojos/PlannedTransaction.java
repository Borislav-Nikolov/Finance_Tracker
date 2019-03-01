package finalproject.financetracker.model.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Entity(name = "planned_transactions")
public class PlannedTransaction{

//    @Column
    private long repeatPeriod;
}
