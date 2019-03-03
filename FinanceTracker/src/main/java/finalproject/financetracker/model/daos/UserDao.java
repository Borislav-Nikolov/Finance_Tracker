package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.utils.ClosableCloser;
import finalproject.financetracker.model.utils.emailing.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Component
public class UserDao {

    public static Long DEFAULT_CATEGORY_USER_ID = null;
    public static String DEFAULT_USER_USERNAME = "Default";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void registerUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(User user) {
        User deletedUser = userRepository.findByUsername(user.getUsername());
        // TODO consider is_deleted column and validations
        String deletedUserValues = "deleted" + (new Random().nextInt(90000) + 10000) + deletedUser.getUserId();
        // TODO implement after password hashing is possible
        String deletedUserPassword = "deleted" + (new Random().nextInt(90000) + 10000) + deletedUser.getUserId();
        deletedUser.setUsername(deletedUserValues);
        deletedUser.setFirstName(deletedUserValues);
        deletedUser.setLastName(deletedUserValues);
        deletedUser.setEmail(deletedUserValues);
        deletedUser.setEmailConfirmed(false);
        deletedUser.setSubscribed(false);
        tokenRepository.delete(tokenRepository.findByUserId(deletedUser.getUserId()));
        userRepository.save(deletedUser);
    }

    /* ----- UPDATE QUERIES ----- */
    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void updateEmail(User user, String newEmail) {
        String sql = "UPDATE final_project.users SET email = ? WHERE user_id = ?;";
        long id = getUserId(user);
        jdbcTemplate.update(sql, newEmail, id);
    }

    /* ----- SELECT QUERIES ----- */

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public User getUserByEmail(String email) {
        User user = getUserByStringParam("email", email);
        return user;
    }

    public ResultSet getAllEmailsToBeNotifiedByReminder() throws SQLException {
        // TODO do not forget to test
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MONTH, -1);
        Date referenceDate = new Date(cal.getTime().getTime());
        PreparedStatement ps = null;
        try {
            ps = jdbcTemplate.getDataSource().getConnection().prepareStatement(
                "SELECT u.email AS email FROM final_project.users AS u " +
                "JOIN final_project.transactions AS t " +
                "ON (u.user_id = t.user_id) " +
                "WHERE u.last_notified < ? AND u.is_subscribed = 1 " +
                "GROUP BY u.user_id " +
                "HAVING MAX(t.execution_date) < ?;"
            );
            ps.setTimestamp(1, new java.sql.Timestamp(referenceDate.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(referenceDate.getTime()));
            return ps.executeQuery();
        } finally {
            ClosableCloser closer = new ClosableCloser(ps, "Prepared statement at UserDao");
            closer.setPriority(Thread.MAX_PRIORITY);
            closer.start();
        }
    }

    public void updateUserLastNotified(String email) {
        User user = userRepository.findByEmail(email);
        user.setLastNotified(new Date());
        userRepository.save(user);
    }

    private User getUserByStringParam(String col, String param) {
        String sql = "SELECT * FROM final_project.users WHERE "+col+" LIKE ?;";
        User user;
            try {
                user = jdbcTemplate.queryForObject(
                        sql, new Object[]{ param }, new BeanPropertyRowMapper<>(User.class));
            } catch (IncorrectResultSizeDataAccessException ex) {
                return null;
            }
        return user;
    }

    public long getUserId(User user) {
        return getUserByUsername(user.getUsername()).getUserId();
    }

}
