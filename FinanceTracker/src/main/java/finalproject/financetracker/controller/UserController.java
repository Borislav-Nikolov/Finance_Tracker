package finalproject.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.dtos.MsgObjectDTO;
import finalproject.financetracker.model.repositories.TokenRepository;
import finalproject.financetracker.model.repositories.UserRepository;
import finalproject.financetracker.model.dtos.CommonMsgDTO;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.dtos.userDTOs.*;
import finalproject.financetracker.model.pojos.VerificationToken;
import finalproject.financetracker.utils.emailing.MailUtil;
import finalproject.financetracker.utils.emailing.OnRegistrationCompleteEvent;
import finalproject.financetracker.utils.passCrypt.PassCrypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

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
    private MailUtil mailUtil;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PassCrypter passCrypter;

    /* ----- STATUS CHANGES ----- */

    @PostMapping(value = "/register")
    public MsgObjectDTO registerUser(@RequestBody RegistrationDTO regInfo, WebRequest request)
            throws InvalidRequestDataException, FailedActionException {
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
            // TODO come up with appropriate handler
            throw new FailedActionException("User registration failed.");
        }
        CommonMsgDTO msg = new CommonMsgDTO("User registered. Verification email successfully sent.", new Date());
        ProfileInfoDTO profile = this.getProfileInfoDTO(user);
        return new MsgObjectDTO(msg, profile);
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
            session.setAttribute(SESSION_USER_KEY, AbstractController.toJson(user));
            session.setAttribute(SESSION_USERNAME_KEY, user.getUsername());
            session.setAttribute(SESSION_IP_ADDR_KEY, request.getRemoteAddr());
            session.setMaxInactiveInterval(-1);
            user.setLastLogin(new Date());
            userRepository.save(user);
            CommonMsgDTO msg = new CommonMsgDTO("Login successful.", new Date());
            ProfileInfoDTO profile = this.getProfileInfoDTO(user);
            return new MsgObjectDTO(msg, profile);
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
        CommonMsgDTO msg = new CommonMsgDTO("Logout successful.", new Date());
        ProfileInfoDTO profile = this.getProfileInfoDTO(user);
        return new MsgObjectDTO(msg, profile);
    }
    @GetMapping(value = "/confirm")
    public CommonMsgDTO confirmEmail(@RequestParam(value = "token") String token) throws MyException {
        VerificationToken verToken = tokenRepository.findByToken(token);
        this.validateToken(verToken);
        User user = userRepository.getByUserId(verToken.getUserId());
        user.setEmailConfirmed(true);
        userDao.verifyUserEmail(user, verToken);
        return new CommonMsgDTO("Email " + user.getEmail() + " was confirmed.", new Date());
    }

    /* ----- PROFILE ACTIONS ----- */

    @GetMapping(value = "/profile")
    public ProfileInfoDTO viewProfile(HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        return getProfileInfoDTO(user);
    }
    @GetMapping(value = "/profile/edit")
    public ProfileInfoDTO editProfile(HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        return getProfileInfoDTO(user);
    }
    // TODO maybe gather editing into one method
    @PutMapping(value = "/profile/edit/password")
    public CommonMsgDTO changePassword(@RequestBody PassChangeDTO passChange, HttpSession session,
                                       HttpServletRequest request)
            throws IOException, MyException {
        passChange.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        if (passChange.getOldPass().equals(user.getPassword())) {
            String newPass = passChange.getNewPass().trim();
            String newPass2 = passChange.getNewPass2().trim();
            validateNewPassword(newPass, newPass2);
            user.setPassword(newPass);
            userDao.updateUser(user);
        } else {
            throw new InvalidRequestDataException("Wrong password.");
        }
        return new CommonMsgDTO("Password changed successfully.", new Date());
    }
    @PutMapping(value = "/profile/edit/email")
    public ProfileInfoDTO changeEmail(@RequestBody EmailChangeDTO emailChange, HttpSession session,
                                      HttpServletRequest request)
            throws IOException, MyException {
        emailChange.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        if (emailChange.getPassword().equals(user.getPassword())) {
            String newEmail = emailChange.getNewEmail().trim();
            this.validateEmail(newEmail);
            user.setEmail(newEmail);
            userDao.updateUser(user);
        } else {
            throw new InvalidRequestDataException("Wrong password.");
        }
        return this.getProfileInfoDTO(user);
    }
    @DeleteMapping(value = "/profile")
    public ProfileInfoDTO deleteProfile(@RequestBody Map<String, String> password, HttpSession session,
                                        HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        if (password.get("password").equals(user.getPassword())) {
            userDao.deleteUser(user);
            session.invalidate();
        } else {
            throw new InvalidRequestDataException("Wrong password.");
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
        // TODO remove after testing
        // added for easier testing
        if (password.charAt(0) == '1') {
            return;
        }
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
        if (user == null || !passCrypter.check(password,user.getPassword())) {
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
    private String formatName(String name) {
        if (name != null && name.isEmpty()) {
            return null;
        }
        return name;
    }
}

