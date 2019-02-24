package finalproject.financetracker.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

@Controller
@RequestMapping(value = "/profile/account")
@ResponseBody
public class AccountController {
    private final AccountDao dao;

    @Autowired
    AccountController(AccountDao dao) {
        this.dao = dao;
    }

    @RequestMapping(value = "/add",
            method = RequestMethod.POST,
            produces = "application/json")
    @ResponseBody
    public Account addAcc(@RequestBody Account a, HttpSession sess)
            throws
            SQLException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            NotLoggedInException,
            IOException {

        //TODO to be removed after testing
        System.out.println("!isLogged " + !UserController.isLoggedIn(sess));
        System.out.println("!isValidAccountData " + !isValidAccount(a));

        if (!isValidAccount(a)) {
            throw new InvalidRequestDataException();
        }
        if (!UserController.isLoggedIn(sess)) {
            throw new NotLoggedInException();
        }
        User u = getUserFromSession(sess);

        if (!(u.getUserId() == a.getUserId())) {

            //TODO to be removed after testing
            System.out.println("a.id " + a.getUserId());
            System.out.println("u.id " + u.getUserId());
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        Account[] checkAcc = dao.getAllAsc(a.getUserId());

        //TODO to be removed after testing
        System.out.println(Arrays.toString(checkAcc));

        for (Account account : checkAcc) {
            if (a.getAccountName().equalsIgnoreCase(account.getAccountName())) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }

        long accId = dao.addAcc(a);

        //TODO to be removed after testing
        System.out.println(a);

        a.setAccountId(accId);
        return a;
    }

    //TODO add more web services and create Exception Handlers

    @RequestMapping(value = "/delete/{accId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteAcc(@PathVariable(name = "accId") long accId,
                          HttpSession sess)
            throws SQLException, IOException, NotLoggedInException, NotFoundException {

        if (!UserController.isLoggedIn(sess)) {
            throw new NotLoggedInException();
        }

        User u = getUserFromSession(sess);

        if (u == null || u.getUserId() <= 0) {
            throw new NotLoggedInException();
        }
        Account account = dao.getById(accId);
        if (account.getUserId()!= u.getUserId()){
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }

        dao.deleteAcc(AccountDao.SQLColumnName.ACCOUNT_ID, AccountDao.SQLCompareOperator.EQUALS, accId);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Account updateAcc(@RequestBody Account a, HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            SQLException,
            ForbiddenRequestException,
            NotFoundException {

        //TODO to be removed after testing
        System.out.println("!isLogged " + !UserController.isLoggedIn(sess));
        System.out.println("!isValidAccountData " + !isValidAccount(a));

        if (!isValidAccount(a) && a.getAccountId()>=0) {
            throw new InvalidRequestDataException();
        }
        if (!UserController.isLoggedIn(sess)) {
            throw new NotLoggedInException();
        }
        User u = getUserFromSession(sess);
        Account account = dao.getById(a.getAccountId());

        if (!(u.getUserId() == account.getUserId() && u.getUserId()==a.getUserId())) {
            //TODO to be removed after testing
            System.out.println("a.id " + a.getUserId());
            System.out.println("u.id " + u.getUserId());
            throw new ForbiddenRequestException("not logged in or account doesn't below to that user");
        }
        Account[] checkAcc = dao.getAllAsc(a.getUserId());

        //TODO to be removed after testing
        System.out.println(Arrays.toString(checkAcc));

        for (Account acc : checkAcc) {
            if (a.getAccountName().equalsIgnoreCase(acc.getAccountName()) && a.getAccountId()!=acc.getAccountId()) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }

        dao.updateAcc(a);
        return a;
    }

    @RequestMapping(value = "/all/{desc}", method = RequestMethod.GET)
    @ResponseBody
    public Account[] allAccOrdered(@RequestBody User u, @PathVariable(name = "desc") String order, HttpSession sess) throws NotLoggedInException, IOException, SQLException, NotFoundException, InvalidRequestDataException {
        if (u== null) throw new InvalidRequestDataException();
        User sessUser = getUserFromSession(sess);
        if (!UserController.isLoggedIn(sess) || sessUser.getUserId() != u.getUserId()) {
            throw new NotLoggedInException();
        }
        if (order!= null && order.equalsIgnoreCase("desc")){
            return dao.getAllDesc(u.getUserId());
        }
        if (order!= null && order.equalsIgnoreCase("asc")){
            return dao.getAllAsc(u.getUserId());
        }
        //TODO remove msg
        throw new NotFoundException("fdasghf");
    }

   /* @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public void allAcc(HttpServletResponse resp) throws NotLoggedInException, IOException, SQLException, NotFoundException {
        resp.setStatus(HttpServletResponse.SC_FOUND);
        resp.sendRedirect("/all/asc");
    }*/

    public static boolean isValidAccount(Account a) {
        return a != null &&
                a.getAccountName() != null &&
                !a.getAccountName().isEmpty() &&
                !(a.getAmount() <= 0) &&
                a.getUserId() > 0;
    }

    public static User getUserFromSession(HttpSession sess) throws NotLoggedInException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        if(sess.isNew()){
            throw new NotLoggedInException();
        }
        User u = mapper.readValue(sess.getAttribute("User").toString(), User.class);
        if (u == null) {
            throw new NotLoggedInException();
        }
        return u;
    }

    public static <T extends Object> String toJson(T u) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(u);
    }


    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public String IOExceptionHandler(IOException e){
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public String SQLExceptionHandler(IOException e){
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public String ExceptionHandler(Exception e) throws Exception {
        if(e instanceof MyException || e instanceof JsonProcessingException){
            throw e;
        }else {
            throw new SeverErrorException();
        }
    }
}
