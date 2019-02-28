package finalproject.financetracker.model.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BudgetDao {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    BudgetRepository budgetRepository;

}
