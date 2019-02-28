package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import finalproject.financetracker.model.dtos.CommonMsgDTO;
import finalproject.financetracker.model.exceptions.AlreadyLoggedInException;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.exceptions.user_exceptions.*;
import finalproject.financetracker.model.dtos.userDTOs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
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
        String username = regInfo.getUsername();
        String password = regInfo.getPassword().trim();
        String password2 = regInfo.getPassword2().trim();
        String firstName = regInfo.getFirstName();
        String lastName = regInfo.getLastName();
        String email = regInfo.getEmail();
        User user = new User(username, password, firstName, lastName, email);
        this.validateUsername(username);
        this.validateEmail(email);
        this.validatePasswordsAtRegistration(user, password2);
        if (firstName != null && firstName.isEmpty()) {
            user.setFirstName(null);
        }
        if (lastName != null && lastName.isEmpty()) {
            user.setLastName(null);
        }
        userDao.registerUser(user);
        return user;
    }
    @PostMapping(value = "/login")
    public LoginRespDTO loginUser(@RequestBody LoginInfoDTO loginInfo, HttpSession session)
                                    throws MyException, JsonProcessingException {
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword().trim();
        User user = userDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            validateLoginAttempt(username, password);
            session.setAttribute("User", AbstractController.toJson(user));
            session.setAttribute("Username", user.getUsername());
            session.setMaxInactiveInterval(-1);
            return new LoginRespDTO(user.getUserId(), user.getUsername(), user.getFirstName(),
                    user.getLastName(), user.getEmail(), new Date());
        } else {
            throw new AlreadyLoggedInException();
        }
    }
    @PostMapping(value = "/logout")
    public CommonMsgDTO logoutUser(HttpSession session) throws AlreadyLoggedOutException {
        if (!isLoggedIn(session)) {
            throw new AlreadyLoggedOutException();
        }
        session.invalidate();
        // TODO research if this response is good practice
        return new CommonMsgDTO("Logout successful.", new Date());
    }

    /* ----- PROFILE ACTIONS ----- */

    @GetMapping(value = "/profile")
    public ProfileInfoDTO viewProfile(HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        return new ProfileInfoDTO(user.getUserId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getEmail());
    }
    @GetMapping(value = "/profile/edit")
    public ProfileInfoDTO editProfile(HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        return new ProfileInfoDTO(user.getUserId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getEmail());
    }
    @PutMapping(value = "/profile/edit/password")
    public CommonMsgDTO changePassword(@RequestBody PassChangeDTO passChange, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        if (passChange.getOldPass().equals(user.getPassword())) {
            validateNewPassword(passChange.getNewPass(), passChange.getNewPass2());
            user.setPassword(passChange.getNewPass());
            userDao.updateUser(user);
        } else {
            throw new InvalidPasswordInputException();
        }
        return new CommonMsgDTO("Password changed successfully.", new Date());
    }
    @PutMapping(value = "/profile/edit/email")
    public CommonMsgDTO changeEmail(@RequestBody EmailChangeDTO emailChange, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        if (emailChange.getPassword().equals(user.getPassword())) {
            this.validateEmail(emailChange.getNewEmail());
            user.setEmail(emailChange.getNewEmail());
            userDao.updateUser(user);
        } else {
            throw new InvalidPasswordInputException();
        }
        return new CommonMsgDTO("Email changed successfully.", new Date());
    }
    @DeleteMapping(value = "/profile/edit/deleteProfile")
    public CommonMsgDTO deleteProfile(@RequestBody Map<String, String> password, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        if (password.get("password").equals(user.getPassword())) {
            userDao.deleteUser(user);
            session.invalidate();
        } else {
            throw new InvalidPasswordInputException();
        }
        return new CommonMsgDTO("Profile deleted successfully.", new Date());
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
                throw new InvalidPasswordException();
            }
            if (!user.getPassword().equals(password2)) {
                throw new PasswordMismatchException();
            }
        }
        private void validateNewPassword(String newPass, String newPass2)
                throws PasswordValidationException {
            if ((newPass == null || newPass2 == null) ||
                    (newPass.length() < 3  || newPass2.length() < 3)) {
                throw new InvalidPasswordException();
            }
            if (!newPass.equals(newPass2)) {
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
    }

