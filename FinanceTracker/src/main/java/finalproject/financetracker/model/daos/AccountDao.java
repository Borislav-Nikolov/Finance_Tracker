package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.SpringJdbcConfig;
import finalproject.financetracker.controller.UserController;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@Component
public class AccountDao {

    public enum AccountParam {
        ACCOUNT_ID, ACCOUNT_NAME, USER_ID
    }

    private final SpringJdbcConfig mySQL;

    AccountDao(SpringJdbcConfig mySQL) {
        this.mySQL = mySQL;
    }

    public void addAcc(Account acc) throws SQLException {
        String sql = "USE final_project; INSERT INTO accounts(account_name, user_id, amount) VALUES (?, ?, ?)";
        PreparedStatement ps = SpringJdbcConfig.mysqlDataSource().getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, acc.getAccountName());
        ps.setLong(2,acc.getUserId());
        ps.setDouble(3, acc.getAmount());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();

        if (rs.next()) {
            acc.setAccountId(rs.getLong(1));
        }
        mySQL.closeResultSet(rs);
        mySQL.closeStatement(ps);
    }





    //TODO ---------------------------- unfinished
    /*@PostMapping("/profile/account/delete/{param}={value}")
    public void deleteAcc(@PathVariable(name = "param") String deleteParam,
                          @PathVariable(name = "value") String value,
                          HttpServletRequest req)
            throws SQLException, InvalidRequestDataException, NotLoggedInException {



        if (!req.getSession().isNew()) {
            boolean delete = false;
            for (AccountParam param : AccountParam.values()) {
                if (param.toString().equalsIgnoreCase(deleteParam)) {
                    delete = true;
                }
            }

            if (!delete) {
                throw new InvalidRequestDataException();
            }

            if (!UserController.checkIfLoggedIn(req.getSession())) {
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
    public ArrayList<Account> getAllAcc(@PathVariable(name = "username") String username,
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
