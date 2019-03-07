package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.daos.TokenDao;
import finalproject.financetracker.model.dtos.MsgObjectDTO;
import finalproject.financetracker.model.repositories.TokenRepository;
import finalproject.financetracker.model.repositories.UserRepository;
import finalproject.financetracker.model.dtos.CommonMsgDTO;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.dtos.userDTOs.*;
import finalproject.financetracker.model.pojos.VerificationToken;
import finalproject.financetracker.utils.emailing.EmailSender;
import finalproject.financetracker.utils.emailing.OnRegistrationCompleteEvent;
import finalproject.financetracker.utils.passCrypt.PassCrypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(produces = "application/json")
public class UserController extends AbstractController {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PassCrypter passCrypter;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private TokenDao tokenDao;

    /* ----- STATUS CHANGES ----- */

    @PostMapping(value = "/register")
    public MsgObjectDTO registerUser(@RequestBody RegistrationDTO regInfo,
                                     HttpServletRequest request, HttpSession session)
            throws InvalidRequestDataException, FailedActionException, AlreadyLoggedInException {
        if (isLoggedIn(session)) {
            throw new AlreadyLoggedInException("Must not be logged in to create new user.");
        }
        regInfo.checkValid();
        String username = regInfo.getUsername().trim();
        String password = regInfo.getPassword().trim();
        String password2 = regInfo.getPassword2().trim();
        String firstName = this.formatName(regInfo.getFirstName().trim());
        String lastName = this.formatName(regInfo.getLastName().trim());
        String email = regInfo.getEmail().trim();
        boolean isSubscribed = regInfo.isSubscribed();
        this.validateUsername(username);
        this.validateEmail(email);
        this.validatePasswordsAtRegistration(password, password2);
        User user = new User(
                username,
                passCrypter.crypt(password),
                firstName,
                lastName,
                email,
                false,
                isSubscribed);
        user.setLastNotified(new Date());
        try {
            userRepository.save(user);
            String appUrl = request.getContextPath();
            applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
        } catch (Exception ex) {
            user = userRepository.findByUsername(user.getUsername());
            if (user != null) {
                userDao.deleteUser(user);
            }
            throw new FailedActionException("User registration failed.");
        }
        ProfileInfoDTO profile = this.getProfileInfoDTO(user);
        return new MsgObjectDTO("User registered. Verification email successfully sent.", new Date(), profile);
    }
    @PostMapping(value = "/login")
    public MsgObjectDTO loginUser(@RequestBody LoginInfoDTO loginInfo, HttpSession session, HttpServletRequest request)
            throws MyException, JsonProcessingException {
        loginInfo.checkValid();
        String username = loginInfo.getUsername().trim();
        String password = loginInfo.getPassword().trim();
        User user = userDao.getUserByUsername(username);
        if (!isLoggedIn(session)) {
            validateLoginAttempt(username, password);
            this.setupSession(session, user, request);
            user.setLastLogin(new Date());
            userRepository.save(user);
            ProfileInfoDTO profile = this.getProfileInfoDTO(user);
            return new MsgObjectDTO("Login successful.", new Date(), profile);
        } else {
            this.validateIpAddr(session, request);
            throw new AlreadyLoggedInException();
        }
    }
    @GetMapping(value = "/logout")
    public MsgObjectDTO logoutUser(HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        session.invalidate();
        ProfileInfoDTO profile = this.getProfileInfoDTO(user);
        return new MsgObjectDTO("Logout successful.", new Date(), profile);
    }
    @GetMapping(value = "/confirm")
    public MsgObjectDTO confirmEmail(@RequestParam(value = "token") String token,
                                     HttpSession session,
                                     HttpServletRequest request)
            throws MyException, JsonProcessingException {
        VerificationToken verToken = tokenRepository.findByToken(token);
        this.validateToken(verToken);
        User user = userRepository.getByUserId(verToken.getUserId());
        user.setEmailConfirmed(true);
        this.setupSession(session, user, request);
        userDao.verifyUserEmail(user, verToken);
        ProfileInfoDTO profile = getProfileInfoDTO(user);
        return new MsgObjectDTO("Email " + user.getEmail() + " was confirmed.", new Date(), profile);
    }
    @GetMapping(value = "/new_token")
    public CommonMsgDTO sendNewToken(HttpSession session, HttpServletRequest request) throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        if ((System.currentTimeMillis() - user.getLastNotified().getTime()) < TokenDao.NEW_TOKEN_INTERVAL) {
            throw new BadRequestException("Cannot send new token yet. Time between new token sending is " +
                    (TokenDao.NEW_TOKEN_INTERVAL / 60 / 1000) + " minutes.");
        }
        user.setLastNotified(new Date());
        this.setupSession(session, user, request);
        this.sendVerificationTokenToUser(user, request);
        return new CommonMsgDTO("Confirmation email sent successfully to " + user.getEmail()  + ".", new Date());
    }

    /* ----- PROFILE ACTIONS ----- */

    @PutMapping(value = "/profile/edit/password")
    public MsgObjectDTO changePassword(@RequestBody PassChangeDTO passChange, HttpSession session,
                                       HttpServletRequest request)
            throws IOException, MyException {
        passChange.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        this.validateUserPasswordInput(passChange.getOldPass(), user.getPassword());
        String newPass = passChange.getNewPass().trim();
        String newPass2 = passChange.getNewPass2().trim();
        validateNewPassword(newPass, newPass2);
        user.setPassword(passCrypter.crypt(newPass));
        this.setupSession(session, user, request);
        userDao.updateUser(user);
        ProfileInfoDTO profile = getProfileInfoDTO(user);
        return new MsgObjectDTO("Password changed successfully.", new Date(), profile);
    }
    @PutMapping(value = "/profile/edit/email")
    public MsgObjectDTO changeEmail(@RequestBody EmailChangeDTO emailChange, HttpSession session,
                                      HttpServletRequest request)
            throws IOException, MyException {
        emailChange.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        this.validateUserPasswordInput(emailChange.getPassword(), user.getPassword());
        String newEmail = emailChange.getNewEmail().trim();
        this.validateEmail(newEmail);
        user.setEmail(newEmail);
        user.setEmailConfirmed(false);
        this.setupSession(session, user, request);
        this.sendVerificationTokenToUser(user, request);
        userDao.updateUser(user);
        ProfileInfoDTO profile = getProfileInfoDTO(user);
        return new MsgObjectDTO("Email changed successfully.", new Date(), profile);
    }
    @DeleteMapping(value = "/profile")
    public MsgObjectDTO deleteProfile(@RequestBody Map<String, String> password, HttpSession session,
                                        HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        this.validateUserPasswordInput(password.get("password"), (user.getPassword()));
        userDao.deleteUser(user);
        session.invalidate();
        ProfileInfoDTO profile = getProfileInfoDTO(user);
        return new MsgObjectDTO("User deleted successfully.", new Date(), profile);
    }
    /* ----- VALIDATIONS ----- */

    public static boolean isLoggedIn (HttpSession session){
        return !(session.isNew() || session.getAttribute("Username") == null);
    }
    private void validateEmail (String email) throws InvalidRequestDataException {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            throw new InvalidRequestDataException("Invalid email given.");
        }
        if (userDao.getUserByEmail(email) != null) {
            throw new InvalidRequestDataException("Email is already taken.");
        }
    }
    private void validatePasswordsAtRegistration(String password, String password2)
            throws InvalidRequestDataException {
        if (password == null || password2 == null) {
            throw new InvalidRequestDataException("Null value for passwords at user registration.");
        }
        if (!password.equals(password2)) {
            throw new InvalidRequestDataException("Passwords don't match.");
        }
        validatePasswordFormat(password);
    }
    private void validateNewPassword(String newPass, String newPass2)
            throws InvalidRequestDataException {
        if (newPass == null || newPass2 == null) {
            throw new InvalidRequestDataException("Null value for passwords at password change.");
        }
        if (!newPass.equals(newPass2)) {
            throw new InvalidRequestDataException("Passwords don't match.");
        }
        validatePasswordFormat(newPass);
    }
    private void validatePasswordFormat(String password) throws InvalidRequestDataException {
        if (!password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
            throw new InvalidRequestDataException("Invalid password format: must contain at least 8 characters, " +
                    "at least one upper case and one lower case letter, at least one number and at least one special " +
                    "character");
        }
    }
    private void validateUsername(String username) throws InvalidRequestDataException {
        if (username.isEmpty() || username.equals(UserDao.DEFAULT_USER_USERNAME)) {
            throw new InvalidRequestDataException("Invalid username input.");
        }
        if (userDao.getUserByUsername(username) != null) {
            throw new InvalidRequestDataException("Username already taken.");
        }
    }
    private void validateLoginAttempt(String username, String password) throws InvalidRequestDataException {
        User user = userDao.getUserByUsername(username);
        if (user == null || !passCrypter.check(password, user.getPassword())) {
            throw new InvalidRequestDataException("Wrong user or password.");
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
    private void validateUserPasswordInput(String givenPass, String userPass) throws InvalidRequestDataException {
        if (!passCrypter.check(givenPass, userPass)) {
            throw new InvalidRequestDataException("Wrong password.");
        }
    }
    /* ----- OTHER METHODS ----- */
    private void setupSession(HttpSession session, User user, HttpServletRequest request)
            throws JsonProcessingException {
        session.setAttribute(SESSION_USER_KEY, AbstractController.toJson(user));
        session.setAttribute(SESSION_USERNAME_KEY, user.getUsername());
        session.setAttribute(SESSION_IP_ADDR_KEY, request.getRemoteAddr());
        session.setMaxInactiveInterval(-1);
    }
    private String formatName(String name) {
        if (name != null && name.isEmpty()) {
            return null;
        }
        return name;
    }
    private void sendVerificationTokenToUser(User user, HttpServletRequest request) {
        String appUrl = request.getContextPath();
        VerificationToken token = tokenDao.getNewToken(user);
        emailSender.sendEmailConfirmationToken(appUrl, token, user);
    }
    private ProfileInfoDTO getProfileInfoDTO(User user) {
        return new ProfileInfoDTO(user.getUserId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.isEmailConfirmed(), user.isSubscribed());
    }
}
