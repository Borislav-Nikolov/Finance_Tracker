package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.SpringJdbcConfig;
import finalproject.financetracker.model.User;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public final class UserDao {
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(SpringJdbcConfig.mysqlDataSource());

    public static void registerUser(User user) {
        String sql = "INSERT INTO final_project.users(username, password, first_name, last_name, email) " +
                "VALUES (?, ?, ?, ?, ?);";
        String username = user.getUsername();
        String password = user.getPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        jdbcTemplate.update(sql, username, password, firstName, lastName, email);
        user.setUserId(getUserId(user));
    }

    public static User getUserByUsername(String username) {
        User user = getUserByStringParam("username", username);
        return user;
    }


    public static User getUserByEmail(String email) {
        User user = getUserByStringParam("email", email);
        return user;
    }


    private static User getUserByStringParam(String col, String param) {
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

    public static int getUserId(User user) {
        return getUserByUsername(user.getUsername()).getUserId();
    }

}
