package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import finalproject.financetracker.model.daos.TokenRepository;
import finalproject.financetracker.model.daos.UserRepository;
import finalproject.financetracker.model.dtos.CommonMsgDTO;
import finalproject.financetracker.model.exceptions.AlreadyLoggedInException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.NotFoundException;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.exceptions.user_exceptions.*;
import finalproject.financetracker.model.dtos.userDTOs.*;
import finalproject.financetracker.model.pojos.VerificationToken;
import finalproject.financetracker.model.utils.emailing.MailUtil;
import finalproject.financetracker.model.utils.emailing.OnRegistrationCompleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(produces = "application/json")
public class UserController extends AbstractController {

    @Autowired
    UserDao userDao;
    @Autowired
    MailUtil mailUtil;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    /* ----- STATUS CHANGES ----- */

    @PostMapping(value = "/register")
    public ProfileInfoDTO registerUser(@RequestBody RegistrationDTO regInfo, WebRequest request)
            throws RegistrationValidationException, InvalidRequestDataException {
        regInfo.checkValid();
        String username = regInfo.getUsername().trim();
        String password = regInfo.getPassword().trim();
        String password2 = regInfo.getPassword2().trim();
        String firstName = regInfo.getFirstName().trim();
        String lastName = regInfo.getLastName().trim();
        String email = regInfo.getEmail().trim();
        boolean isSubscribed = regInfo.isSubscribed();
        User user = new User(username, password, firstName, lastName, email, false, isSubscribed);
        this.validateUsername(username);
        this.validateEmail(email);
        this.validatePasswordsAtRegistration(user, password2);
        this.formatNames(user);
        userDao.registerUser(user);
        String appUrl = request.getContextPath();
        applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
        return getProfileInfoDTO(user);
    }
    @PostMapping(value = "/login")
    public LoginRespDTO loginUser(@RequestBody LoginInfoDTO loginInfo, HttpSession session)
                                    throws MyException, JsonProcessingException {
        loginInfo.checkValid();
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword().trim();
        User user = userDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            validateLoginAttempt(username, password);
            session.setAttribute("User", AbstractController.toJson(user));
            session.setAttribute("Username", user.getUsername());
            session.setMaxInactiveInterval(-1);
            return new LoginRespDTO(user.getUserId(), user.getUsername(), user.getFirstName(),
                    user.getLastName(), user.getEmail(), user.isEmailConfirmed(), user.isSubscribed(), new Date());
        } else {
            throw new AlreadyLoggedInException();
        }
    }
    @GetMapping(value = "/logout")
    public CommonMsgDTO logoutUser(HttpSession session) throws AlreadyLoggedOutException {
        if (!isLoggedIn(session)) {
            throw new AlreadyLoggedOutException();
        }
        session.invalidate();
        return new CommonMsgDTO("Logout successful.", new Date());
    }
    @GetMapping(value = "/confirm")
    public CommonMsgDTO confirmEmail(@RequestParam(value = "token") String token) throws MyException {
        VerificationToken verToken = tokenRepository.findByToken(token);
        this.validateToken(verToken);
        User user = userRepository.getByUserId(verToken.getUserId());
        user.setEmailConfirmed(true);
        userRepository.save(user);
        tokenRepository.delete(verToken);
        return new CommonMsgDTO("Email " + user.getEmail() + " was confirmed.", new Date());
    }

    /* ----- PROFILE ACTIONS ----- */

    @GetMapping(value = "/profile")
    public ProfileInfoDTO viewProfile(HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        return getProfileInfoDTO(user);
    }
    @GetMapping(value = "/profile/edit")
    public ProfileInfoDTO editProfile(HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        return getProfileInfoDTO(user);
    }
    // TODO maybe gather editing into one method
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
    @DeleteMapping(value = "/profile")
    public ProfileInfoDTO deleteProfile(@RequestBody Map<String, String> password, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        if (password.get("password").equals(user.getPassword())) {
            userDao.deleteUser(user);
            session.invalidate();
        } else {
            throw new InvalidPasswordInputException();
        }
        return getProfileInfoDTO(user);
    }

    private ProfileInfoDTO getProfileInfoDTO(User user) {
        return new ProfileInfoDTO(user.getUserId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.isEmailConfirmed(), user.isSubscribed());
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
        validatePasswordFormat(user.getPassword());
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
        validatePasswordFormat(newPass);
    }
    private void validatePasswordFormat(String password) throws InvalidPasswordException {
        // TODO remove after testing
        // added for easier testing
        if (password.charAt(0) == '1') {
            return;
        }
        if (!password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
            throw new InvalidPasswordException("Invalid password format: must contain at least 8 characters, " +
                    "at least one upper case and one lower case letter, at least one number and at least one special " +
                    "character");
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
    private void validateToken(VerificationToken token) throws MyException {
        if (token == null) {
            throw new NotFoundException("Token was not found.");
        }
        if (token.getExpiryDate().before(new Date())) {
            tokenRepository.delete(token);
            throw new InvalidRequestDataException("That token has already expired.");
        }
    }
    private void formatNames(User user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        if (firstName != null && firstName.isEmpty()) {
            user.setFirstName(null);
        }
        if (lastName != null && lastName.isEmpty()) {
            user.setLastName(null);
        }
    }
}

