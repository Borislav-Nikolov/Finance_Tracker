package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlannedTransactionDao extends AbstractDao {
    private JdbcTemplate jdbcTemplate;

    PlannedTransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PlannedTransaction> getAllWhereExecDateBeofreNext24Hours() {
        String sql = "SELECT * FROM final_project.planned_transactions " +
                "WHERE next_execution_date < DATE(NOW()+ INTERVAL 24 HOUR)";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PlannedTransaction.class));
    }

    public List<ReturnPlannedTransactionDTO> getAllByAccIdIsIncomeOrder(
            long userId,
            Long accId,
            Long catId,
            Boolean isIncome,
            SQLColumnName columnName,
            SQLOderBy orderBy) {
        String incomeQuery = (isIncome != null) ? ("AND c.is_income = " + isIncome + " ") : " ";
        String accIdQuery = (accId != null) ? ("AND a.account_id = " + accId + " ") : " ";
        String catIdQuery = (catId != null) ? ("AND c.category_id = " + catId + " ") : " ";

        String sql = "SELECT " +
                "u.username," +
                "u.user_id, " +
                "a.account_name," +
                "a.account_id," +
                "pt.pt_id AS transaction_id," +
                "pt.pt_name AS transaction_name, " +
                "pt.pt_amount AS amount," +
                "pt.next_execution_date," +
                "pt.repeat_period," +
                "c.category_id, " +
                "c.category_name, " +
                "c.is_income " +
                "FROM accounts AS a " +
                "JOIN " +
                "planned_transactions AS pt ON pt.account_id = a.account_id " +
                "JOIN " +
                "categories AS c ON pt.category_id = c.category_id " +
                "JOIN " +
                "users AS u ON a.user_id = u.user_id " +
                "WHERE a.user_id = ? " +                              //(1 -> userId) Long
                 accIdQuery +
                 catIdQuery +
                 incomeQuery +
                "ORDER BY " + columnName.toString() + " " + orderBy.toString() + ";";
        System.out.println(sql);  //TODO {remove}show sql query in console
        return jdbcTemplate.query(
                sql,
                new Object[]{userId},
                new BeanPropertyRowMapper<>(ReturnPlannedTransactionDTO.class));
    }
}
