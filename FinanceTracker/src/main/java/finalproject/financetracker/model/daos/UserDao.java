package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.SpringJdbcConfig;
import finalproject.financetracker.model.User;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public final class UserDao {
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(SpringJdbcConfig.mysqlDataSource());
    private UserDao(){}
    public static void registerUser(User user) {
        insertUserIntoChosenTable("users", user);
        user.setUserId(getUserId(user));
    }

    public static void deleteUser(User user) {
        insertUserIntoChosenTable("deleted_users", user);
        String deleteSQL = "DELETE FROM final_project.users WHERE user_id = ?;";
        long userID = getUserId(user);
        jdbcTemplate.update(deleteSQL, userID);

    }

    private static void insertUserIntoChosenTable(String table, User user) {
        String sql = "INSERT INTO final_project." + table + "(username, password, first_name, last_name, email) " +
            "VALUES (?, ?, ?, ?, ?);";
        String username = user.getUsername();
        String password = user.getPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        jdbcTemplate.update(sql, username, password, firstName, lastName, email);
    }

    public static void insertAdmin() {
        long adminId = 1;
        String adminName = "Admin";
        String password = "adminpass123";
        String firstName = "Big";
        String lastName = "Boss";
        String email = "admin@fintrack.ft";
        if (getUserByUsername(adminName) != null) return;
        jdbcTemplate.update(
                "INSERT INTO final_project.users(user_id, username, password, first_name, last_name, email) " +
                        "VALUES (?, ?, ?, ?, ?, ?);", adminId, adminName, password, firstName, lastName, email
        );
    }

    /* ----- UPDATE QUERIES ----- */
    public static void updatePassword(User user, String newPass) {
        String sql = "UPDATE final_project.users SET password = ? WHERE user_id = ?;";
        long id = getUserId(user);
        jdbcTemplate.update(sql, newPass, id);
    }

    public static void updateEmail(User user, String newEmail) {
        String sql = "UPDATE final_project.users SET email = ? WHERE user_id = ?;";
        long id = getUserId(user);
        jdbcTemplate.update(sql, newEmail, id);
    }

    /* ----- SELECT QUERIES ----- */

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

    public static long getUserId(User user) {
        return getUserByUsername(user.getUsername()).getUserId();
    }

}
