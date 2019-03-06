package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.AbstractController;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Predicate;

@Component
public class TransactionDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<ReturnTransactionDTO> getAllByAccIdStartDateEndDateIsIncome(
            Long userId,
            Long accId,
            Long startDateTimestamp,
            Long endDateTimestamp,
            Boolean isIncome,
            AbstractDao.SQLColumnName columnName,
            AbstractDao.SQLOderBy order){
        String orIncome = (isIncome!=null)?isIncome+") ":"true OR c.is_income = false)";

        String orAcc = (accId!=null)? accId+") ":(" 1 OR a.account_id != 1 )");

        String sql = "SELECT " +
                "u.username," +
                "u.user_id, " +
                "a.account_name," +
                "a.account_id," +
                "t.transaction_id," +
                "t.transaction_name, " +
                "t.amount," +
                "t.execution_date," +
                "c.category_id, " +
                "c.category_name, " +
                "c.is_income " +
                "FROM accounts AS a " +
                "JOIN " +
                "transactions AS t ON t.account_id = a.account_id " +
                "JOIN " +
                "categories AS c ON t.category_id = c.category_id " +
                "JOIN " +
                "users AS u ON a.user_id = u.user_id "+
                "WHERE a.user_id = ? " +                              //(1 -> userId) Long
                "AND t.execution_date >= FROM_UNIXTIME(?) " +         //(2 -> startDate) Long(sec)
                "AND t.execution_date < FROM_UNIXTIME(?) " +          //(3 -> endDate) Long(sec)
                "AND (a.account_id = " +orAcc+
                " AND (c.is_income = " + orIncome +
                " ORDER BY "+columnName.toString()+" "+order.toString()+";";
        System.out.println(sql);  //TODO {remove}show sql query in console
        return jdbcTemplate.query(
                sql,
                new Object[]
                    {
                        userId,
                        startDateTimestamp/ AbstractController.SEC_TO_MILIS,
                        endDateTimestamp/AbstractController.SEC_TO_MILIS
                    },
                new BeanPropertyRowMapper<>(ReturnTransactionDTO.class));
    }
}
