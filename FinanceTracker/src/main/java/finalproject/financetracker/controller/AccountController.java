package finalproject.financetracker.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@Controller
@RequestMapping(value = "/profile/accounts")
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

    //--------------get account---------------------//
    @RequestMapping(value = "/{accId}", method = RequestMethod.GET)
    @ResponseBody
    public Account getAccById(@PathVariable(name = "accId") long accId,
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
        return account;
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
    @RequestMapping(value = "/all/{asc/desc}", method = RequestMethod.GET)
    @ResponseBody
    public Account[] allAccOrdered(@PathVariable(name = "asc/desc") String order,
                                   HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            SQLException,
            NotFoundException,
            InvalidRequestDataException {

        User sessUser = getLoggedUserWithIdFromSession(sess);

        if (order != null && order.equalsIgnoreCase("desc")) {
            return dao.getAllDesc(sessUser.getUserId());
        }
        if (order != null && order.equalsIgnoreCase("asc")) {
            return dao.getAllAsc(sessUser.getUserId());
        }
        //TODO remove msg
        throw new NotFoundException("st: not found");
    }

    //--------------get all accounts for a given userId---------------------//
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public void allAcc(HttpServletResponse resp,
                       HttpSession sess) throws IOException, InvalidRequestDataException, NotLoggedInException {

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.sendRedirect("/profile/accounts/all/asc");
    }

    //--------------get total account number for a given userId---------------------//
    @RequestMapping(value = "/all/count", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode allAccCount(HttpSession sess)
            throws
            InvalidRequestDataException,
            SQLException,
            NotLoggedInException,
            IOException {

        User u = getLoggedUserWithIdFromSession(sess);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jn = mapper.createObjectNode();
        long accounts = dao.getAllCount(u.getUserId());
        jn.put("userId", u.getUserId());
        jn.put("accounts", accounts);
        return jn;
    }
    //-----------------------< /Web Services >----------------------//


}
