package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BudgetDao {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    BudgetRepository budgetRepository;

}
