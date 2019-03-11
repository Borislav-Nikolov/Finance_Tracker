package finalproject.financetracker.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.exceptions.ForbiddenRequestException;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.daos.PlannedTransactionDao;
import finalproject.financetracker.model.dtos.account.AddAccountDTO;
import finalproject.financetracker.model.dtos.account.EditAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnUserBalanceDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.repositories.AccountRepo;
import finalproject.financetracker.model.repositories.PlannedTransactionRepo;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/profile", produces = "application/json")
@ResponseBody
public class AccountController extends AbstractController {

    @Autowired
    private AccountDao dao;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private TransactionRepo tRepo;
    @Autowired
    private PlannedTransactionRepo ptRepo;
    @Autowired
    private PlannedTransactionDao ptDao;
    @Autowired
    private PlannedTransactionController plannedTransactionController;


    private void checkIfAccountWithSuchNameExists(List<Account> accounts, String accName)
            throws ForbiddenRequestException {

        for (Account account : accounts) {
            if (accName.trim().equalsIgnoreCase(account.getAccountName().trim())) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }
    }

    private void checkIfAccountWithSuchNameAndDiffIdExists(List<Account> accounts, String accName, long accId)
            throws ForbiddenRequestException {

        for (Account account : accounts) {
            if (accName.trim().equalsIgnoreCase(account.getAccountName().trim()) && accId != account.getUserId()) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }
    }
    //-----------------------< Web Services >----------------------//

    //--------------add account for given user---------------------//
    @RequestMapping(value = "/accounts",
            method = RequestMethod.POST)
    public ReturnAccountDTO addAcc(@RequestBody AddAccountDTO a,
                                   HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            IOException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        a.checkValid();
        List<Account> checkAcc = dao.getAllAccountsAsc(u.getUserId());
        checkIfAccountWithSuchNameExists(checkAcc, a.getAccountName());
        Account returnAcc = new Account(
                a.getAccountName(),
                a.getAmount(),
                u.getUserId());
        long accId = dao.addAcc(returnAcc);
        returnAcc.setAccountId(accId);
        return new ReturnAccountDTO(returnAcc)
                .withUser(u);
    }

    //--------------get account---------------------//
    @RequestMapping(
            value = "/accounts/{accId}",
            method = RequestMethod.GET)
    public ReturnAccountDTO getAccById(@PathVariable(value = "accId") String accId,
                                       HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            IOException,
            MyException {

        long idL = parseLong(accId);
        return getAccByIdLong(idL, sess, request);
    }

    ReturnAccountDTO getAccByIdLong(long accId,
                                    HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            IOException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        Account account = dao.getById(accId);
        checkIfNotNull(Account.class, account);
        checkIfBelongsToLoggedUser(account.getUserId(), u);
        return new ReturnAccountDTO(account)
                .withUser(u);
    }

    //--------------delete account---------------------//
    @RequestMapping(
            value = "/accounts/{accId}",
            method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public ReturnAccountDTO deleteAcc(@PathVariable(name = "accId") String accId,
                                      HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            IOException,
            MyException {

        ReturnAccountDTO a = getAccById(accId, sess, request);
        ptRepo.deleteAllByAccountId(a.getAccountId());
        tRepo.deleteByAccountId(a.getAccountId());
        accountRepo.deleteById(a.getAccountId());
        return a;
    }

    //--------------update account---------------------//
    @RequestMapping(
            value = "/accounts",
            method = RequestMethod.PUT)
    public ReturnAccountDTO editAccount(@RequestBody EditAccountDTO a,
                                        HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        a.checkValid();
        ReturnAccountDTO returnAccountDTO = getAccByIdLong(a.getAccountId(), sess, request);
        List<Account> allUserAccounts = dao.getAllAccountsAsc(u.getUserId());
        checkIfAccountWithSuchNameAndDiffIdExists(allUserAccounts, a.getAccountName(), a.getAccountId());
        dao.updateAcc(a);
        returnAccountDTO.setAccountName(a.getAccountName());
        return returnAccountDTO.withUser(u);

    }

    //--------------show all accounts for a given userId ascending/descending---------------------//
    @RequestMapping(
            value = "/accounts",
            method = RequestMethod.GET)
    public List<ReturnAccountDTO> allAccOrdered(@RequestParam(value = "desc", required = false) Boolean order,
                                                HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        List<Account> result;                                     // ORDER BY Account name
        if (order != null && order) {
            result = dao.getAllAccountsDesc(u.getUserId());
        } else {
            result = dao.getAllAccountsAsc(u.getUserId());
        }
        return result
                .stream()
                .map(account ->
                        new ReturnAccountDTO(account)
                                .withUser(u))
                .collect(Collectors.toList());
    }

    //--------------get total account number for a given userId---------------------//
    @RequestMapping(value = "/accounts/count", method = RequestMethod.GET)
    public JsonNode allAccCount(HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            IOException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jn = mapper.createObjectNode();
        long accounts = dao.getAllCount(u.getUserId());
        jn.put("userId", u.getUserId());
        jn.put("accounts", accounts);
        return jn;
    }


    //-------------- get User balance all accounts amount ---------------------//
    @RequestMapping(value = "/balance", method = RequestMethod.GET)
    public ReturnUserBalanceDTO getBalance(HttpSession sess, HttpServletRequest request)
            throws
            SQLException,
            MyException,
            IOException {

        User u = getLoggedValidUserFromSession(sess, request);
        return new ReturnUserBalanceDTO(u).withBalance(dao.getUserBalanceByUserId(u.getUserId()));
    }
    //-----------------------< /Web Services >----------------------//


    //-----------------------< Account scheduled Task >----------------------//
    @Scheduled(cron = "00 00 00 * * *")
    //<second> <minute> <hour> <day-of-month> <month> <day-of-week> {optional}<year>
    void executePlannedTransactions() {
        plannedTransactionController.startScheduledCheck(LocalDate.now().plusDays(1));
    }

    //-----------------------< /Scheduled Task >----------------------//


}
