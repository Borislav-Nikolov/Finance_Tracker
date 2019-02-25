package finalproject.financetracker.controller;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@Controller
@RequestMapping(value = "/profile/account")
public class AccountController extends AbstractController{
    private final AccountDao dao;

    @Autowired
    AccountController(AccountDao dao) {
        this.dao = dao;
    }

    //-----------------------< Web Services >----------------------//

    //--------------add account for given user---------------------//
    @RequestMapping(value = "/add",
            method = RequestMethod.POST,
            produces = "application/json")
    @ResponseBody
    public Account addAcc(@RequestBody Account a,
                          HttpSession sess)
            throws
            SQLException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            NotLoggedInException,
            IOException {

        if (!isValidAccount(a)) {
            throw new InvalidRequestDataException();
        }
        User u = getLoggedUserWithIdFromSession(sess);

        if (!(u.getUserId() == a.getUserId())) {
            //todo change msg
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        Account[] checkAcc = dao.getAllAsc(a.getUserId());

        for (Account account : checkAcc) {
            if (a.getAccountName().equalsIgnoreCase(account.getAccountName())) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }
        long accId = dao.addAcc(a);
        a.setAccountId(accId);
        return a;
    }

    //--------------delete account---------------------//
    @RequestMapping(value = "/delete/{accId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteAcc(@PathVariable(name = "accId") long accId,
                          HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException, InvalidRequestDataException {

        User u = getLoggedUserWithIdFromSession(sess);
        Account account = dao.getById(accId);

        if (account.getUserId() != u.getUserId()) {
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        dao.deleteAcc(AccountDao.SQLColumnName.ACCOUNT_ID, AccountDao.SQLCompareOperator.EQUALS, accId);
    }

    //--------------update account---------------------//
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public Account updateAcc(@RequestBody Account a,
                             HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            SQLException,
            ForbiddenRequestException,
            NotFoundException {

        if (!isValidAccount(a)) {
            throw new InvalidRequestDataException();
        }
        User u = getLoggedUserWithIdFromSession(sess);
        Account account = dao.getById(a.getAccountId());

        if (!(u.getUserId() == account.getUserId() && u.getUserId() == a.getUserId())) {

            throw new ForbiddenRequestException("not logged in or account doesn't below to that user");
        }
        Account[] checkAcc = dao.getAllAsc(a.getUserId());

        for (Account acc : checkAcc) {
            if (a.getAccountName().equalsIgnoreCase(acc.getAccountName()) && a.getAccountId() != acc.getAccountId()) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }
        dao.updateAcc(a);
        return a;
    }

    //--------------show all accounts for a given userId ascending/descending---------------------//
    @RequestMapping(value = "/all/{userId}/{desc}", method = RequestMethod.GET)
    @ResponseBody
    public Account[] allAccOrdered(@PathVariable(name = "userId") long userId,
                                   @PathVariable(name = "desc") String order,
                                   HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            SQLException,
            NotFoundException,
            InvalidRequestDataException {

        if (userId <= 0) {
            throw new InvalidRequestDataException();
        }
        User sessUser = getLoggedUserWithIdFromSession(sess);

        if (sessUser.getUserId() != userId) {
            throw new NotLoggedInException();
        }
        if (order != null && order.equalsIgnoreCase("desc")) {
            return dao.getAllDesc(userId);
        }
        if (order != null && order.equalsIgnoreCase("asc")) {
            return dao.getAllAsc(userId);
        }
        //TODO remove msg
        throw new NotFoundException("st: not found");
    }

    //--------------get all accounts for a given userId---------------------//
    @RequestMapping(value = "/all/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public void allAcc(@PathVariable(name = "userId") long userId,
                       HttpServletResponse resp,
                       HttpSession sess) throws IOException, InvalidRequestDataException, NotLoggedInException {

        if (userId <= 0) {
            throw new InvalidRequestDataException();
        }

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.sendRedirect("/profile/account/all/" + userId + "/asc");
    }

    //--------------get total account number for a given userId---------------------//
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode allAccCount(@RequestParam(value = "id", required = true) long id,
                                HttpSession sess)
            throws
            InvalidRequestDataException,
            SQLException,
            NotLoggedInException,
            IOException {

        if (id <= 0) {
            throw new InvalidRequestDataException();
        }
        if (getLoggedUserWithIdFromSession(sess).getUserId() != id){
            throw new NotLoggedInException();
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jn = mapper.createObjectNode();
        long accounts = dao.getAllCount(id);
        jn.put("userId", id);
        jn.put("accounts", accounts);
        return jn;
    }
    //-----------------------< /Web Services >----------------------//


}
