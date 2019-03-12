package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.dtos.account.EditAccountDTO;
import finalproject.financetracker.model.pojos.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class AccountDao extends AbstractDao {

    private Connection mySQLCon;
    private final JdbcTemplate jdbcTemplate;
    private static final ReentrantLock connLock = new ReentrantLock();

    @Autowired
    AccountDao(JdbcTemplate jdbcTemplate) throws SQLException {
        this.jdbcTemplate = jdbcTemplate;
        checkConnAndReInitIfBad();
    }

    private void checkConnAndReInitIfBad( ) throws SQLException {
        synchronized (AccountDao.connLock) {
            if (this.mySQLCon == null || this.mySQLCon.isClosed()) {
                logger.info("MySQLCon in AccountDao is created.");
                this.mySQLCon = this.jdbcTemplate.getDataSource().getConnection();
            }
        }
    }

    @PreDestroy
    void closeMySQLCon() throws SQLException {
        try {
            if (this.mySQLCon!= null) {
                this.mySQLCon.close();
                logger.info("MySQLCon in AccountDao is closed.");
            }
        } catch (SQLException e) {
            logger.error("Error closing mySQLCon in AccountDao. Trying again...");
            this.mySQLCon.close();
            logger.info("MySQLCon in AccountDao is closed.");
        }
    }

    public void updateAcc(EditAccountDTO acc) throws SQLException {
        PreparedStatement ps = null;
        checkConnAndReInitIfBad();
        try {
            String sql = "UPDATE final_project.accounts SET account_name = ? WHERE account_id = ?;";
            ps = mySQLCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, acc.getAccountName().trim());
            ps.setLong(2, acc.getAccountId());
            ps.executeUpdate();
        }
        finally {
            closeStatement(ps);
        }
    }

    public int getAllCount(long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        checkConnAndReInitIfBad();
        int result;
        try {
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM final_project.accounts AS a " +
                    "WHERE a.user_id  = ?;";

            ps = mySQLCon.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            result = -1;

            if (rs.next()) {
                result = rs.getInt(1);
            }
        }
        finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
        if (result<0) throw new SQLException("error retrieving ResultSet");
        return result;
    }

    public List<Account> getAllAccountsAsc(long userId) throws SQLException {
        return getAll(userId, QUERY_RETURN_MAX_LIMIT, QUERY_RETURN_OFFSET_DEFAULT, SQLOrder.ASC);
    }

    public List<Account> getAllAccountsDesc(long userId) throws SQLException {
        return getAll(userId, QUERY_RETURN_MAX_LIMIT, QUERY_RETURN_OFFSET_DEFAULT, SQLOrder.DESC);
    }

    private List<Account> getAll(long userId, int limit, int offset, SQLOrder order) throws SQLException {
        return getAllWhere(SQLOrderBy.USER_ID, SQLCompareOperator.EQUALS, userId, limit, offset, order);
    }

    private List<Account> getAllWhere(
            SQLOrderBy param,
            SQLCompareOperator operator,
            long idColumnValueLong,
            int limit,
            int offset,
            SQLOrder order)
            throws
            SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        checkConnAndReInitIfBad();
        ArrayList<Account> arr;
        try {
            String sql =
                    "SELECT a.account_id, a.account_name, ROUND(a.amount,2) AS amount, a.user_id " +
                            "FROM final_project.accounts AS a " +
                            "WHERE a." + param.toString() + " " + operator.getValue() + " ? " +
                            "ORDER BY a.account_name " + order + " LIMIT ? OFFSET ?;";

            ps = mySQLCon.prepareStatement(sql);
            ps.setLong(1, idColumnValueLong);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            rs = ps.executeQuery();
            arr = new ArrayList<>();

            while (rs.next()) {
                arr.add(new Account(
                        rs.getLong("account_id"),
                        rs.getString("account_name"),
                        rs.getDouble("amount"),
                        rs.getLong("user_id"))
                );
            }
        }
        finally {
            closeStatement(ps);
            closeResultSet(rs);
        }
        return arr;
    }

    public long addAcc(Account acc) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        checkConnAndReInitIfBad();
        long accId;
        try {
            String sql = "INSERT INTO final_project.accounts(account_name, user_id, amount) VALUES (?, ?, ?);";
            ps = mySQLCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, acc.getAccountName().trim());
            ps.setLong(2, acc.getUserId());
            ps.setDouble(3, acc.getAmount());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            accId = -1;

            if (rs.next()){
                accId = rs.getLong(1);
            }
        }
        finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
        if (accId<=0) {
            throw new SQLException("error retrieving data from data base");
        }
        return accId;
    }

    public Account getById(long id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        checkConnAndReInitIfBad();
        try {
            String sql =
                    "SELECT a.account_id, a.account_name, ROUND(a.amount,2), a.user_id " +
                            "FROM final_project.accounts AS a " +
                            "WHERE a.account_id = ?;";

            ps = mySQLCon.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return new Account(
                        rs.getLong("account_id"),
                        rs.getString("account_name"),
                        rs.getDouble("amount"),
                        rs.getLong("user_id"));
            }
        }
        finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
        return null;
    }

    public double getUserBalanceByUserId(long userId) throws SQLException {
        String sql = "SELECT SUM(a.amount) AS sum FROM final_project.accounts AS a WHERE a.user_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        checkConnAndReInitIfBad();
        try {
            ps = mySQLCon.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()){
                return rs.getDouble("sum");
            }else return 0;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }
}
