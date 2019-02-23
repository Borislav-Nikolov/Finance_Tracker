package finalproject.financetracker.controller;

import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.UserDao;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
public class UserController {

    @PostMapping(value ="/register")
    public User registerUser(@RequestBody RegistrationInfo pass2,
                             HttpServletResponse resp) throws IOException {
        User user = pass2.user;
        String password2 = pass2.password2;
        if (user.getPassword() == null || password2 == null) {
            resp.setStatus(400);
            resp.getWriter().append("Input matching passwords.");
            return null;
        }
        try {
            if (!this.checkIfUserOrEmailExist(user) &&
                password2.equals(user.getPassword()) &&
                this.validateEmail(user.getEmail())) {
                if (user.getFirstName().isEmpty()) {
                    user.setFirstName(null);
                }
                if (user.getLastName().isEmpty()) {
                    user.setLastName(null);
                }
                UserDao.registerUser(user);
//                resp.sendRedirect(); - to login form
            } else {
                resp.setStatus(400);
                resp.getWriter().append("Input matching passwords.");
                return null;
            }
        } catch (RegistrationCheckException ex) {
            resp.setStatus(400);
            resp.getWriter().append(ex.getMessage());
            return null;
        }
        user.setUserId(UserDao.getUserByUsername(user.getUsername()).getUserId());
        System.out.println("-------------------------- " + UserDao.getUserByUsername(user.getUsername()).getUserId() + " ----------------------------------");
        return user;
    }

    @PostMapping(value = "/login")
    public void loginUser(@RequestBody LoginInfo loginInfo, HttpServletResponse resp, HttpSession session) throws IOException {
        String username = loginInfo.username;
        String password = loginInfo.password;
        User user = UserDao.getUserByUsername(username);
        if (!checkIfLoggedIn(session)) {
            if (UserDao.getUserByUsername(username) == null ||
                !user.getPassword().equals(password)) {
                resp.setStatus(400);
                resp.getWriter().append("Wrong user or password.");
            } else {

            }
        } else {
            // TODO already logged in
        }
    }

    public static boolean checkIfLoggedIn(HttpSession session) {
        // TODO
        return false;
    }

    private boolean checkIfUserOrEmailExist(User user) throws RegistrationCheckException {
        if (UserDao.getUserByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistsException("User already exists.");
        } else if (UserDao.getUserByEmail(user.getEmail()) != null) {
            throw new EmailAlreadyUsedException("Email already used.");
        }
        return false;
    }

    private boolean validateEmail(String email) throws InvalidEmailException {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            throw new InvalidEmailException("Invalid email address.");
        }
        return result;
    }

    private static class RegistrationInfo {
        private String password2;
        private User user;
        RegistrationInfo(String username, String password, String firstName, String lastName, String email, String password2) {
            this.user = new User(username, password, firstName, lastName, email);
            this.password2 = password2;
        }
    }

    private static class LoginInfo {
        private String username;
        private String password;
        private LoginInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private static class EmailAlreadyUsedException extends RegistrationCheckException {
        private EmailAlreadyUsedException(String message) {
            super(message);
        }
    }
    private static class InvalidEmailException extends RegistrationCheckException {
        private InvalidEmailException(String message) {
            super(message);
        }
    }
    private static class UserAlreadyExistsException extends RegistrationCheckException {
        private UserAlreadyExistsException(String message) {
            super(message);
        }
    }
    private static class RegistrationCheckException extends Exception {
        private RegistrationCheckException(String message) {
            super(message);
        }
    }

}
