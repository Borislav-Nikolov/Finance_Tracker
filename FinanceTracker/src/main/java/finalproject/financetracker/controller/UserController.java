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
        return user;
    }

    @PostMapping(value = "/login")
    public User loginUser(@RequestBody LoginInfo loginInfo, HttpServletResponse resp, HttpSession session) throws IOException {
        String username = loginInfo.username;
        String password = loginInfo.password;
        User user = UserDao.getUserByUsername(username);
        if (!checkIfLoggedIn(session)) {
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
            // TODO resp.sendRedirect("");
            return null;
        }
    }

    @PostMapping(value = "/logout")
    public void logoutUser(HttpSession session, HttpServletResponse resp) throws IOException {
        if (checkIfLoggedIn(session)) {
            session.invalidate();
            resp.getWriter().append("You've logged out successfully.");
            // TODO resp.sendRedirect("");
        } else {
            resp.getWriter().append("You're already logged out.");
            // TODO resp.sendRedirect("");
        }
    }


    @GetMapping(value = "/profile/{username}")
    public User viewProfile(@PathVariable("username") String username, HttpSession session, HttpServletResponse resp)
                            throws IOException {
        User user = UserDao.getUserByUsername(username);
        if (!checkIfLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            // TODO resp.sendRedirect("");
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
    public User changePassword(@RequestBody NewPassword newPass, HttpSession session, HttpServletResponse resp) throws IOException {
        if (!checkIfLoggedIn(session)) {
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            // TODO resp.sendRedirect("");
            return null;
        } else {
            User user = UserDao.getUserByUsername(session.getAttribute("Username").toString());
            // TODO not finished
        }
        return null;
    }


    public static boolean checkIfLoggedIn(HttpSession session) {
        if (session.isNew()) {
            return false;
        } else if (session.getAttribute("User") == null) {
            return false;
        }
        return true;
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

    private static class NewPassword {
        private String oldPass;
        private String newPass;
        private String newPass2;
        public NewPassword(String oldPass, String newPass, String newPass2) {
            this.oldPass = oldPass;
            this.newPass = newPass;
            this.newPass2 = newPass2;
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
