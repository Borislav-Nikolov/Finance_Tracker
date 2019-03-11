package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import lombok.ToString;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@ToString
public class PlannedTransactionDao extends AbstractDao {
    private JdbcTemplate jdbcTemplate;

    public PlannedTransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PlannedTransaction> getAllPlannedTransactionBefore(LocalDate localDate) {
        long currentDate = localDate
                .atTime(0,0,0).toEpochSecond(ZoneOffset.ofHoursMinutes(2,0));
        String sql =
                "SELECT pt.pt_id, " +
                "pt.pt_name, " +
                "pt.next_execution_date, " +
                "pt.pt_amount, " +
                "pt.account_id, " +
                "pt.category_id, " +
                "pt.repeat_period, " +
                "a.account_name, " +
                "c.category_name, " +
                "c.is_income, " +
                "a.user_id AS user_id " +
                "FROM final_project.planned_transactions AS pt " +
                "JOIN accounts AS a ON pt.account_id = a.account_id " +
                "JOIN categories AS c ON pt.category_id = c.category_id " +
                "WHERE next_execution_date < FROM_UNIXTIME("+currentDate+")";
        System.out.println(sql);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PlannedTransaction.class));
    }

    public List<ReturnPlannedTransactionDTO> getAllByAccIdIsIncomeOrder(
            long userId,
            Long accId,
            Long catId,
            Boolean isIncome,
            SQLOrderBy orderBy,
            SQLOrder order) {
        String incomeQuery = (isIncome != null) ? ("AND c.is_income = " + isIncome + " ") : " ";
        String accIdQuery = (accId != null) ? ("AND a.account_id = " + accId + " ") : " ";
        String catIdQuery = (catId != null) ? ("AND c.category_id = " + catId + " ") : " ";
        String orderInQuery = (order != null) ? (" "+order.toString() + " ") : "";
        String orderByInQuery = (orderBy != null) ? ("ORDER BY " + orderBy.toString() + orderInQuery) : " ";

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
                 orderByInQuery + ";";
        System.out.println(sql);
        return jdbcTemplate.query(
                sql,
                new Object[]{userId},
                new BeanPropertyRowMapper<>(ReturnPlannedTransactionDTO.class));
    }
}
