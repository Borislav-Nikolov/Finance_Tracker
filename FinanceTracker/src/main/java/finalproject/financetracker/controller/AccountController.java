package finalproject.financetracker.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.sql.SQLException;

@Controller
@RequestMapping(value = "/profile")
public class AccountController extends AbstractController{
    private final AccountDao dao;

    @Autowired
    AccountController(AccountDao dao) {
        this.dao = dao;
    }

    //-----------------------< Web Services >----------------------//

    //--------------add account for given user---------------------//
    @RequestMapping(value = "/accounts/add",
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

        User u = getLoggedUserWithIdFromSession(sess);
        if (!isValidAccount(a)) {
            throw new InvalidRequestDataException();
        }

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
    @RequestMapping(
            value = "/accounts/delete/{accId}",
            method = RequestMethod.DELETE)
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
    @RequestMapping(
            value = "/accounts/",
            method = RequestMethod.GET)
    @ResponseBody
    public Account getAccById(@RequestParam("accID") String accId,
                          HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException, InvalidRequestDataException {
        long longAccId = -1;
        User u = getLoggedUserWithIdFromSession(sess);

        try {
           longAccId = Long.parseLong(accId);
        }catch (Exception e){
            throw new InvalidRequestDataException("account id not provided");
        }
        Account account = dao.getById(longAccId);

        if (account.getUserId() != u.getUserId()) {
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        return account;
    }


    public Account getAccByIdLong(long accId, HttpSession sess)
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
    @RequestMapping(
            value = "/accounts/update",
            method = RequestMethod.PUT)
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

        User u = getLoggedUserWithIdFromSession(sess);
        if (!isValidAccount(a)) {
            throw new InvalidRequestDataException();
        }

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
    @RequestMapping(
            value = "/accounts/all/{asc}",
            method = RequestMethod.GET)
    @ResponseBody
    public Account[] allAccOrdered(@PathVariable("asc") String order,
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
    @RequestMapping(
            value = "/accounts/all",
            method = RequestMethod.GET)
    @ResponseBody
    public void allAcc(HttpServletResponse resp) throws IOException{

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.sendRedirect("/profile/accounts/all/asc");
    }

    //--------------get total account number for a given userId---------------------//
    @RequestMapping(value = "/accounts/all/count", method = RequestMethod.GET)
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
