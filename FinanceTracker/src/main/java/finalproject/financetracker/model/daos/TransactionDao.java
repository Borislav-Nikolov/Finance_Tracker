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
            Long accId,//null deafault
            Long catId,//null deafault
            Long startDateTimestamp,
            Long endDateTimestamp,
            Boolean isIncome,//null default
            AbstractDao.SQLOrderBy orderBy,
            AbstractDao.SQLOrder order,
            Integer limitInt,
            Integer offsetInt) {
        String incomeQuery = (isIncome != null) ? ("AND c.is_income = " + isIncome + " ") : " ";
        String accIdQuery = (accId != null) ? ("AND a.account_id = " + accId + " ") : " ";
        String catIdQuery = (catId != null) ? ("AND c.category_id = " + catId + " ") : " ";
        String orderInQuery = (order != null) ? (" " + order.toString() + " ") : "";
        String orderByInQuery = (orderBy != null) ? ("ORDER BY " + orderBy.toString() + orderInQuery + " ") : " ";
        String offsetInQuery = (offsetInt != null) ? ("OFFSET " + offsetInt + " ") : " ";
        String limitInQuery = (limitInt != null) ? ("LIMIT " + limitInt + " " + offsetInQuery + " ") : " ";
        String endDateInQuery = (endDateTimestamp != null) ?
                ("AND t.execution_date < FROM_UNIXTIME(" + (endDateTimestamp/AbstractController.SEC_TO_MILLIS) + ") ")
                : " ";

        String sql = "SELECT " +
                "u.username," +
                "u.user_id, " +
                "a.account_name," +
                "a.account_id," +
                "t.transaction_id," +
                "t.transaction_name, " +
                "ROUND(t.amount,2) AS amount," +
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
                "users AS u ON a.user_id = u.user_id " +
                "WHERE a.user_id = ? " +                              //(1 -> userId) Long
                "AND t.execution_date >= FROM_UNIXTIME(?) " +         //(2 -> startDate) Long(sec)
                endDateInQuery +
                accIdQuery +
                incomeQuery +
                catIdQuery +
                orderByInQuery +
                limitInQuery + ";";
        System.out.println(sql);
        return jdbcTemplate.query(
                sql,
                new Object[]
                        {
                                userId,
                                startDateTimestamp / AbstractController.SEC_TO_MILLIS,  // FROM_UNIXTIME accepts seconds SQL
                        },
                new BeanPropertyRowMapper<>(ReturnTransactionDTO.class));
    }
}
