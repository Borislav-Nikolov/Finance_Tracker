package finalproject.financetracker.controller;

import finalproject.financetracker.model.exceptions.AlreadyLoggedInException;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.exceptions.user_exceptions.*;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@RestController
public class UserController extends AbstractController {

    @Autowired
    UserDao userDao;

    @GetMapping(value = "/")
    public void goToIndex(HttpServletResponse resp, HttpSession session) throws IOException {
        if (!isLoggedIn(session)) {
            resp.sendRedirect("/index.html");
        } else {
            resp.sendRedirect("/logged.html");
        }
    }

    /* ----- STATUS CHANGES ----- */

    @PostMapping(value = "/register")
    public User registerUser(@RequestBody RegistrationDTO regInfo) throws RegistrationValidationException {
        User user = regInfo.user;
        String password2 = regInfo.password2;
        this.validateUsername(user.getUsername());
        this.validateEmail(user.getEmail());
        this.validatePasswordsAtRegistration(user, password2);
        if (user.getFirstName() != null && user.getFirstName().isEmpty()) {
            user.setFirstName(null);
        }
        if (user.getLastName() != null && user.getLastName().isEmpty()) {
            user.setLastName(null);
        }
        userDao.registerUser(user);
        return user;
    }

    @PostMapping(value = "/login")
    public User loginUser(@RequestBody LoginDTO loginInfo, HttpSession session) throws Exception {
        String username = loginInfo.username;
        String password = loginInfo.password;
        User user = userDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            validateLoginAttempt(username, password);
            session.setAttribute("User", AccountController.toJson(user));
            session.setAttribute("Username", user.getUsername());
            session.setMaxInactiveInterval(-1);
            return user;
        } else {
            throw new AlreadyLoggedInException();
        }
    }

    @PostMapping(value = "/logout")
    public String logoutUser(HttpSession session) throws AlreadyLoggedOutException {
        if (!isLoggedIn(session)) {
            throw new AlreadyLoggedOutException();
        }
        session.invalidate();
        return "Logout successful.";
    }

    /* ----- PROFILE ACTIONS ----- */

    @GetMapping(value = "/profile/{username}")
    public User viewProfile(@PathVariable("username") String username, HttpSession session, HttpServletResponse resp)
            throws Exception {
        User user = userDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            throw new NotLoggedInException();
        }
        /* --- MAYBE (probably not) --- */
        // fill accounts
        // TODO user.setAccounts(Accoun);
        // fill categories
        // TODO user.setCategories(CategoryDao.getAllCategories());
        /* --- MAYBE (probably not) --- */
        return user;
    }

    @PutMapping(value = "/profile/edit/password")
    public User changePassword(@RequestBody NewPassword newPass,
                               HttpSession session,
                               HttpServletResponse resp) throws Exception {
        User user;
        if (!isLoggedIn(session)) {
            throw new NotLoggedInException();
        } else {
            user = userDao.getUserByUsername(session.getAttribute("Username").toString());
            if (newPass.oldPass.equals(user.getPassword()) &&
                    newPass.newPass.equals(newPass.newPass2) &&
                    !newPass.newPass.isEmpty()) {
                user.setPassword(newPass.newPass);
                userDao.updateUser(user);
            } else {
                throw new InvalidPasswordAtRegistrationException();
            }
        }
        return user;
    }

    @PutMapping(value = "/profile/edit/email")
    public User changeEmail(@RequestBody NewEmail newEmail, HttpSession session, HttpServletResponse resp) throws Exception {
        User user;
        if (!isLoggedIn(session)) {
            throw new NotLoggedInException();
        } else {
            user = userDao.getUserByUsername(session.getAttribute("Username").toString());
            if (newEmail.password.equals(user.getPassword())) {
                if (userDao.getUserByEmail(newEmail.newEmail) == null) {
                    this.validateEmail(newEmail.newEmail);
                    user.setEmail(newEmail.newEmail);
                    userDao.updateUser(user);
                } else {
                    throw new EmailAlreadyUsedException();
                }
            } else {
                throw new InvalidPasswordInputException();
            }
        }
        return user;
    }

    @DeleteMapping(value = "/profile/edit/deleteProfile")
    public void deleteProfile(@RequestBody Map<String, String> password, HttpSession session, HttpServletResponse resp)
            throws Exception {
        if (!isLoggedIn(session)) {
            throw new NotLoggedInException();
        } else {
            String username = session.getAttribute("Username").toString();
            User user = userDao.getUserByUsername(username);
            if (password.get("password").equals(user.getPassword())) {
                userDao.deleteUser(user);
                session.invalidate();
                resp.sendRedirect("/index.html");
            } else {
                throw new InvalidPasswordInputException();
            }
        }
    }

        /* ----- VALIDATIONS ----- */

        public static boolean isLoggedIn (HttpSession session){
            return !(session.isNew() || session.getAttribute("Username") == null);
        }

        private void validateEmail (String email) throws RegistrationValidationException {
            try {
                InternetAddress emailAddr = new InternetAddress(email);
                emailAddr.validate();
            } catch (AddressException ex) {
                throw new InvalidEmailException();
            }
            if (userDao.getUserByEmail(email) != null) {
                throw new EmailAlreadyUsedException();
            }
        }

        private void validatePasswordsAtRegistration(User user, String password2)
                throws RegistrationValidationException {
            if ((user.getPassword() == null || password2 == null) ||
                    (user.getPassword().length() < 3  || password2.length() < 3)) {
                throw new InvalidPasswordAtRegistrationException();
            }
            if (!user.getPassword().equals(password2)) {
                throw new PasswordMismatchException();
            }
        }

        private void validateUsername(String username) throws RegistrationValidationException {
            // TODO make a more intricate validation (concerning abusive language, etc.)
            if (username.isEmpty() || username.equals(UserDao.DEFAULT_USER_USERNAME)) {
                throw new InvalidUsernameException();
            }
            if (userDao.getUserByUsername(username) != null) {
                throw new UserAlreadyExistsException();
            }
        }

        private void validateLoginAttempt(String username, String password) throws InvalidLoginInfoException {
            User user = userDao.getUserByUsername(username);
            if (user == null || !user.getPassword().equals(password)) {
                throw new InvalidLoginInfoException();
            }
        }

        /* ----- INNER CLASSES ----- */

        /* ----- DTO classes ----- */

        private static class RegistrationDTO {
            private String password2;
            // TODO think about separating user into different fields
            private User user;

            private RegistrationDTO(String username, String password, String firstName, String lastName, String email, String password2) {
                this.user = new User(username, password, firstName, lastName, email);
                this.password2 = password2;
            }
        }

        private static class LoginDTO {
            private String username;
            private String password;
            // TODO make LoginResponseDTO
            // TODO check if works without args constructor
            private LoginDTO(String username, String password) {
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
        private static class SuccessfulLogoutDTO {
            private String msg = "Logout successful";
        }
    }

