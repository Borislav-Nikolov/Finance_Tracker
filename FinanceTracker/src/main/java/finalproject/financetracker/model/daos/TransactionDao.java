package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.AbstractController;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<ReturnTransactionDTO> getAllByAccIdStartDateEndDateIsIncome(
            Long userId,
            Long accId,
            Long catId,
            Long startDateTimestamp,
            Long endDateTimestamp,
            Boolean isIncome,
            AbstractDao.SQLColumnName columnName,
            AbstractDao.SQLOderBy order,
            Integer limitInt,
            Integer offsetInt){
        String incomeQuery = (isIncome!=null)?("AND c.is_income = "+isIncome+" "):" ";
        String accIdQuery = (accId!=null)? ("AND a.account_id = "+accId+" "):" ";
        String catIdQuery = (catId!=null)? ("AND c.category_id = "+catId+" "):" ";

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
                 accIdQuery+
                 incomeQuery +
                 catIdQuery+
                "ORDER BY "+ columnName.toString()+" "+
                 order.toString()+" " +
                "LIMIT "+limitInt+" " +
                "OFFSET "+offsetInt+" ;";
        System.out.println(sql);  //TODO {remove}show sql query in console
        return jdbcTemplate.query(
                sql,
                new Object[]
                    {
                        userId,
                        startDateTimestamp/ AbstractController.SEC_TO_MILLIS,
                        endDateTimestamp/AbstractController.SEC_TO_MILLIS
                    },
                new BeanPropertyRowMapper<>(ReturnTransactionDTO.class));
    }
}
