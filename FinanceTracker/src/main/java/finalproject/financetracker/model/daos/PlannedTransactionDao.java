package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.PlannedTransaction;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlannedTransactionDao extends AbstractDao {
    private JdbcTemplate jdbcTemplate;

    PlannedTransactionDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    public List<PlannedTransaction> getAllWhereExecDateEqualsToday() {
        String sql = "SELECT * FROM final_project.planned_transactions WHERE next_execution_date >= CURRENT_TIMESTAMP AND next_execution_date <= DATE(NOW()+ INTERVAL 24 HOUR)";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(PlannedTransaction.class));
    }


}
