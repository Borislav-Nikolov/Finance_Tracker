package finalproject.financetracker.controller;

import finalproject.financetracker.controller.security.SecSecurityConfig;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.UserDao;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping(value = "/")
    public void goToIndex(HttpServletResponse resp, HttpSession session) throws IOException {
        if (!isLoggedIn(session)) {
            resp.sendRedirect("/login.html");
        } else {
            String username = session.getAttribute("Username").toString();
            resp.getWriter().append("Hello, " + username + ".");
        }
    }

    /* ----- STATUS CHANGES ----- */

    @PostMapping(value ="/register")
    public User registerUser(@RequestBody RegistrationInfo pass2,
                             HttpServletResponse resp) throws IOException {
        User user = pass2.user;
        String password2 = pass2.password2;
        if ((user.getPassword() == null || password2 == null) ||
           (user.getPassword().isEmpty() || password2.isEmpty())) {
            resp.setStatus(400);
            resp.getWriter().append("Input valid passwords.");
            resp.sendRedirect("/register.html");
            return null;
        }
        try {
            if (!this.checkIfUserOrEmailExist(user) &&
                password2.equals(user.getPassword()) &&
                this.validateEmail(user.getEmail())) {
                if (user.getFirstName() != null && user.getFirstName().isEmpty()) {
                    user.setFirstName(null);
                }
                if (user.getLastName() != null && user.getLastName().isEmpty()) {
                    user.setLastName(null);
                }
//                user.setPassword(SecSecurityConfig.getEncodedPassword(user.getPassword())); TODO connected to the SecSecurityConfig class
                UserDao.registerUser(user);
                resp.sendRedirect("/login.html");
            } else {
                resp.setStatus(400);
                resp.getWriter().append("Input matching passwords.");
                return null;
            }
        } catch (RegistrationCheckException ex) {
            resp.setStatus(400);
            resp.getWriter().append(ex.getMessage());
            resp.sendRedirect("/register.html");
            return null;
        }
        return user;
    }

    @PostMapping(value = "/login")
    public User loginUser(@RequestBody LoginInfo loginInfo, HttpServletResponse resp, HttpSession session) throws IOException {
        String username = loginInfo.username;
        String password = loginInfo.password;
        User user = UserDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            if (UserDao.getUserByUsername(username) == null ||
                !user.getPassword().equals(password)) {
                resp.setStatus(400);
                resp.getWriter().append("Wrong user or password.");
                return null;
            } else {
                session.setAttribute("User", user);
                session.setAttribute("Username", user.getUsername());
                session.setMaxInactiveInterval(60);
                return user;
            }
        } else {
            resp.getWriter().append("You are already logged in.");
//            resp.sendRedirect("/index.html");
            return null;
        }
    }

    @PostMapping(value = "/logout")
    public void logoutUser(HttpSession session, HttpServletResponse resp) throws IOException {
        if (isLoggedIn(session)) {
            session.invalidate();
            resp.getWriter().append("You've logged out successfully.");
            resp.sendRedirect("/index.html");
        } else {
            resp.getWriter().append("You're already logged out.");
            resp.sendRedirect("/index.html");
        }
    }

    /* ----- PROFILE ACTIONS ----- */

    @GetMapping(value = "/profile/{username}")
    public User viewProfile(@PathVariable("username") String username, HttpSession session, HttpServletResponse resp)
                            throws IOException {
        User user = UserDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            resp.sendRedirect("/login.html");
            return null;
        }
        /* --- MAYBE (probably not) --- */
        // fill accounts
        // TODO user.setAccounts(AccountDao.getAllAcc());
        // fill categories
        // TODO user.setCategories(CategoryDao.getAllCategories());
        /* --- MAYBE (probably not) --- */
        return user;
    }

    @PostMapping(value = "/profile/edit/password")
    public User changePassword(@RequestBody NewPassword newPass,
                               HttpSession session,
                               HttpServletResponse resp) throws IOException {
        User user;
        if (!isLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            resp.sendRedirect("/login.html");
            return null;
        } else {
            user = UserDao.getUserByUsername(session.getAttribute("Username").toString());
            if (newPass.oldPass.equals(user.getPassword()) &&
                    newPass.newPass.equals(newPass.newPass2)) {
                user.setPassword(newPass.newPass);
                UserDao.updatePassword(user, newPass.newPass);
            }
        }
        return user;
    }

    @PostMapping(value = "/profile/edit/email")
    public User changeEmail(@RequestBody NewEmail newEmail, HttpSession session, HttpServletResponse resp) throws IOException {
        User user;
        if (!isLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            resp.sendRedirect("/login.html");
            return null;
        } else {
            user = UserDao.getUserByUsername(session.getAttribute("Username").toString());
            if (newEmail.password.equals(user.getPassword())) {
                user.setEmail(newEmail.newEmail);
                UserDao.updateEmail(user, newEmail.newEmail);
            }
        }
        return user;
    }

    @PostMapping(value = "/profile/edit/deleteProfile")
    public void deleteProfile(@RequestBody Map<String, String> password, HttpSession session, HttpServletResponse resp)
                                throws IOException {
        if (!isLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            resp.sendRedirect("/login.html");
            return;
        } else {
            String username = session.getAttribute("Username").toString();
            User user = UserDao.getUserByUsername(username);
            if (password.get("password").equals(user.getPassword())) {
                UserDao.deleteUser(user);
                resp.sendRedirect("/index.html");
            }
        }
    }

    /* ----- VALIDATIONS ----- */

    public static boolean isLoggedIn(HttpSession session) {
        return !(session.isNew() || session.getAttribute("User") == null);
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

    /* ----- INNER CLASSES ----- */

    /* ----- DTO classes ----- */

    private static class RegistrationInfo {
        private String password2;
        private User user;
        private RegistrationInfo(String username, String password, String firstName, String lastName, String email, String password2) {
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

    private static class NewPassword {
        private String oldPass;
        private String newPass;
        private String newPass2;
        private NewPassword(String oldPass, String newPass, String newPass2) {
            this.oldPass = oldPass;
            this.newPass = newPass;
            this.newPass2 = newPass2;
        }
    }

    private static class NewEmail {
        private String password;
        private String newEmail;
        private NewEmail(String password, String newEmail) {
            this.password = password;
            this.newEmail = newEmail;
        }
    }

    /* ----- UserController specific exceptions ----- */

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
