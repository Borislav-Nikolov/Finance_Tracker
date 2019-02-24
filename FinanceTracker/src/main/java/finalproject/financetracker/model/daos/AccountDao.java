package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.SpringJdbcConfig;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.exceptions.NotFoundException;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@Repository
public class AccountDao {

    public static final int QUERY_RETURN_MAX_LIMIT = 1000;
    public static final int QUERY_RETURN_LIMIT_DEFAULT = 20;
    public static final int QUERY_RETURN_OFFSET_DEFAULT = 0;
    private final SpringJdbcConfig mySQL;

    AccountDao(SpringJdbcConfig mySQL) {
        this.mySQL = mySQL;
    }

    public void updateAcc(Account acc) throws SQLException {
        String sql = "USE final_project; UPDATE accounts SET account_name = ?, amount = ?;";
        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, acc.getAccountName());
        ps.setDouble(2, acc.getAmount());
        ps.executeUpdate();
        mySQL.closeStatement(ps);
    }

    public enum SQLCompareOperator {

        SMALLER_OR_EQUAL("<="), EQUALS("="), BIGGER_OR_EQUAL(">=");

        private String value;

        SQLCompareOperator(String value){
            this.value = value;
        }

        private String getValue(){
            return this.value;
        }
    }

    public enum SQLColumnName {
        ACCOUNT_ID, ACCOUNT_NAME, AMOUNT, USER_ID
    }

    public enum SQLOderBy {
        ASC, DESC
    }

    public int getAllCount(long userId) throws SQLException {
        String sql = "USE final_project; " +
                "SELECT COUNT(*) AS count " +
                "FROM final_project.accounts AS a " +
                "WHERE u.user_id  = ?;";

        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setLong(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        mySQL.closeStatement(ps);
        mySQL.closeResultSet(rs);
        return 0;
    }

    public Account[] getAllAsc(long userId) throws SQLException {
        return getAll(userId,QUERY_RETURN_MAX_LIMIT,QUERY_RETURN_OFFSET_DEFAULT, SQLOderBy.DESC);
    }

    public Account[] getAllDesc(long userId) throws SQLException {
        return getAll(userId,QUERY_RETURN_MAX_LIMIT,QUERY_RETURN_OFFSET_DEFAULT,SQLOderBy.DESC);
    }

    private Account[] getAll(long userId, int limit, int offset, SQLOderBy order) throws SQLException {
        return getAllWhere(SQLColumnName.USER_ID, SQLCompareOperator.EQUALS,userId, limit,offset,order);
    }

    private Account[] getAllWhere(SQLColumnName param, SQLCompareOperator operator, long idColumnValueLong, int limit, int offset, SQLOderBy order) throws SQLException {
        String sql =
                "SELECT a.account_id, a.account_name, a.amount, a.user_id " +
                "FROM final_project.accounts AS a " +
                "WHERE a."+param.toString()+" "+operator.getValue()+" ? ORDER BY a.account_name "+ order +" LIMIT ? OFFSET ?;";

        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setLong(1, idColumnValueLong);
        ps.setInt(2, limit);
        ps.setInt(3, offset);
        ResultSet rs = ps.executeQuery();
        ArrayList<Account> arr = new ArrayList<>();

        while (rs.next()) {
            arr.add(new Account(
                    rs.getLong("account_id"),
                    rs.getString("account_name"),
                    rs.getDouble("amount"),
                    rs.getLong("user_id"))
            );
        }
        mySQL.closeStatement(ps);
        mySQL.closeResultSet(rs);
        return arr.toArray(new Account[0]);
    }

    public long addAcc(Account acc) throws SQLException {
        String sql = "USE final_project; INSERT INTO accounts(account_name, user_id, amount) VALUES (?, ?, ?);";
        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, acc.getAccountName());
        ps.setLong(2,acc.getUserId());
        ps.setDouble(3, acc.getAmount());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        long accId = 0;
        if (rs.next()) {
            accId = rs.getLong(1);
        }
        mySQL.closeResultSet(rs);
        mySQL.closeStatement(ps);
        return accId;
    }

    public int deleteAcc(SQLColumnName param, SQLCompareOperator operator, long idColumn) throws SQLException{
        String sql =
                "DELETE a FROM final_project.accounts AS a " +
                "WHERE a."+param.toString()+" "+operator.getValue()+" ?;";

        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setLong(1, idColumn);
        int affectedRows = ps.executeUpdate();
        mySQL.closeStatement(ps);
        return affectedRows;
    }

    public Account getById(long id) throws SQLException, NotFoundException {
        String sql =
                "SELECT a.account_id, a.account_name, a.amount, a.user_id " +
                        "FROM final_project.accounts AS a " +
                        "WHERE a.account_id = ?;";

        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        ArrayList<Account> arr = new ArrayList<>();

        if (rs.next()) {
            return new Account(
                    rs.getLong("account_id"),
                    rs.getString("account_name"),
                    rs.getDouble("amount"),
                    rs.getLong("user_id"));
        }
        mySQL.closeStatement(ps);
        mySQL.closeResultSet(rs);
        throw new NotFoundException("account "+id+" not found");
    }

    //TODO ---------------------------- unfinished
    /*@PostMapping("/profile/account/delete/{param}={value}")
    public void deleteAcc(@PathVariable(name = "param") String deleteParam,
                          @PathVariable(name = "value") String value,
                          HttpServletRequest req)
            throws SQLException, InvalidRequestDataException, NotLoggedInException {



        if (!req.getSession().isNew()) {
            boolean delete = false;
            for (SQLColumnName param : SQLColumnName.values()) {
                if (param.toString().equalsIgnoreCase(deleteParam)) {
                    delete = true;
                }
            }

            if (!delete) {
                throw new InvalidRequestDataException();
            }

            if (!UserController.isLoggedIn(req.getSession())) {
                throw new NotLoggedInException();
            }

            String sql = "USE final_project; DELETE FROM accounts WHERE " + deleteParam + " = ? ";
            PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

            ps.setString(1, value);
            ps.executeUpdate();
            mySQL.closeStatement(ps);
        }
    }

    @PostMapping(value = "/profile/account/update")
    public void updateAcc(Account acc) throws SQLException {
        String sql = "USE final_project; UPDATE accounts SET account_name = ? , amount = ?";
        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setString(1, acc.getAccountName());
        ps.setDouble(2, acc.getAmount());
        ps.executeUpdate();
        mySQL.closeStatement(ps);
    }

    @GetMapping(value = "/profile/account/{username}/{limit}/{offset}")
    public ArrayList<Account> getAll(@PathVariable(name = "username") String username,
                                        @PathVariable(name = "limit") String limit,
                                        @PathVariable(name = "offset") String offset)
            throws SQLException, InvalidRequestDataException {

        if (!limit.matches("[0-9]") || !offset.matches("[0-9]")) {
            throw new InvalidRequestDataException();
        }

        int intLimit = Integer.parseInt(limit);
        int intOffset = Integer.parseInt(offset);

        if (intLimit <= 0) {
            throw new InvalidRequestDataException();
        }

        String sql = "USE final_project; " +
                "SELECT a.account_id, a.account_name, a.amount, a.user_id " +
                "FROM final_project.accounts AS a JOIN final_project.users AS u ON a.user_id = u.user_id " +
                "WHERE u.username  = ? LIMIT ? OFFSET ?";

        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql);

        ps.setString(1, username);
        ps.setInt(2, intLimit);
        ps.setInt(3, intOffset);
        ResultSet rs = ps.executeQuery();
        ArrayList<Account> arr = new ArrayList<>();
        Account account = new Account();
        while (rs.next()) {
            arr.add(new Account(
                    rs.getLong("account_id"),
                    rs.getString("account_name"),
                    rs.getDouble("amount"),
                    rs.getLong("userId"))
            );
        }
        mySQL.closeStatement(ps);
        return arr;
    }*/
}
