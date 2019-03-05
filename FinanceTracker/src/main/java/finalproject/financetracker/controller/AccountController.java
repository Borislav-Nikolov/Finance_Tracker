package finalproject.financetracker.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.exceptions.ForbiddenRequestException;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.exceptions.NotLoggedInException;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.daos.PlannedTransactionDao;
import finalproject.financetracker.model.dtos.account.AddAccountDTO;
import finalproject.financetracker.model.dtos.account.EditAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnUserBalanceDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.repositories.AccountRepo;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.PlannedTransactionRepo;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/profile", produces = "application/json")
@ResponseBody
public class AccountController extends AbstractController {
    public static final int SEC_TO_MILIS = 1000;

    @Autowired private AccountDao dao;
    @Autowired private AccountRepo accountRepo;
    @Autowired private TransactionRepo tRepo;
    @Autowired private TransactionController transactionController;
    @Autowired private PlannedTransactionRepo ptRepo;
    @Autowired private PlannedTransactionDao ptDao;
    @Autowired private PlannedTransactionController plannedTransactionController;


    private void checkIfAccountWithSuchNameExists(Account[] accounts, String accName)
            throws ForbiddenRequestException {

        for (Account account : accounts) {
            if (accName.trim().equalsIgnoreCase(account.getAccountName().trim())) {
                throw new ForbiddenRequestException("account with such name exists");
            }
        }
    }

    private void checkIfAccountWithSuchNameAndDiffIdExists(Account[] accounts, String accName, long accId)
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
                                   HttpSession sess)
            throws
            SQLException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            NotLoggedInException,
            IOException {

        User u = getLoggedValidUserFromSession(sess);
        a.checkValid();
        Account[] checkAcc = dao.getAllAccountsAsc(u.getUserId());
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
                                       HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException,
            InvalidRequestDataException,
            ForbiddenRequestException {

        long idL = parseNumber(accId);
        return getAccByIdLong(idL, sess);
    }

    ReturnAccountDTO getAccByIdLong(long accId,
                                    HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException,
            ForbiddenRequestException {

        User u = getLoggedValidUserFromSession(sess);
        Account account = dao.getById(accId);
        checkIfNotNull(Account.class, account);
        checkIfBelongsToLoggedUser(account.getUserId(), u);
        List<Transaction> transactions = tRepo.findAllByAccountId(accId);
        List<PlannedTransaction> plannedTransactions = ptRepo.findAllByAccountId(accId);
        return new ReturnAccountDTO(account)
                .withUser(u)
                .withTransactions(transactionController.listEntitiesToListDTOs(transactions, u))
                .withPlannedTransactions(plannedTransactionController.listEntitiesToListDTOs(plannedTransactions, u));
    }

    //--------------delete account---------------------//
    @RequestMapping(
            value = "/accounts/{accId}",
            method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public ReturnAccountDTO deleteAcc(@PathVariable(name = "accId") String accId,
                                      HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException,
            InvalidRequestDataException,
            ForbiddenRequestException {

        ReturnAccountDTO a = getAccById(accId, sess);
        ptRepo.deleteAllByAccountId(a.getAccountId());   //   "/accounts/{accId}  Web Service
        tRepo.deleteByAccountId(a.getAccountId());
        accountRepo.deleteById(a.getAccountId());
        return a;
    }

    //--------------update account---------------------//
    @RequestMapping(
            value = "/accounts",
            method = RequestMethod.PUT)
    public ReturnAccountDTO editAccount(@RequestBody EditAccountDTO a,
                                        HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            SQLException,
            ForbiddenRequestException,
            NotFoundException {

        User u = getLoggedValidUserFromSession(sess);
        a.checkValid();
        ReturnAccountDTO returnAccountDTO = getAccByIdLong(a.getAccountId(), sess);
        Account[] allUserAccounts = dao.getAllAccountsAsc(u.getUserId());
        checkIfAccountWithSuchNameAndDiffIdExists(allUserAccounts, a.getAccountName(), a.getAccountId());
        dao.updateAcc(a);
        returnAccountDTO.setAccountName(a.getAccountName());
        return returnAccountDTO.withUser(u);

    }

    //--------------show all accounts for a given userId ascending/descending---------------------//
    @RequestMapping(
            value = "/accounts",
            method = RequestMethod.GET)
    public List<ReturnAccountDTO> allAccOrdered(@RequestParam(value = "desc", required = false) boolean order,
                                                HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        Account[] result;
        if (order) {
            result = dao.getAllAccountsDesc(u.getUserId());
        } else {
            result = dao.getAllAccountsAsc(u.getUserId());
        }
        return Arrays.stream(result)
                .map(account ->
                        new ReturnAccountDTO(account)
                                .withUser(u)
                                .withPlannedTransactions(
                                        plannedTransactionController.listEntitiesToListDTOs(
                                                ptRepo.findAllByAccountId(account.getAccountId()), u))
                                .withTransactions(
                                        transactionController.listEntitiesToListDTOs(
                                                tRepo.findAllByAccountId(account.getAccountId()), u))
                ).collect(Collectors.toList());
    }

    //--------------get total account number for a given userId---------------------//
    @RequestMapping(value = "/accounts/count", method = RequestMethod.GET)
    public JsonNode allAccCount(HttpSession sess)
            throws
            SQLException,
            NotLoggedInException,
            IOException {

        User u = getLoggedValidUserFromSession(sess);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jn = mapper.createObjectNode();
        long accounts = dao.getAllCount(u.getUserId());
        jn.put("userId", u.getUserId());
        jn.put("accounts", accounts);
        return jn;
    }


    //-------------- get User balance all accounts amount ---------------------//
    @RequestMapping(value = "/balance", method = RequestMethod.GET)
    public ReturnUserBalanceDTO getBalance(HttpSession sess)
            throws
            SQLException,
            NotLoggedInException,
            IOException {

        User u = getLoggedValidUserFromSession(sess);
        return new ReturnUserBalanceDTO(u).withBalance(dao.getUserBalanceByUserId(u.getUserId()));
    }
    //-----------------------< /Web Services >----------------------//


    //-----------------------< Account scheduled Task >----------------------//
    @Scheduled(cron = "0 0 0 * * *")
    //<second> <minute> <hour> <day-of-month> <month> <day-of-week> {optional}<year>
    void executePlannedTransactions() {
        logInfo("Scheduled planned transactions check.");
        List<PlannedTransaction> plannedTransactions = ptDao.getAllWhereExecDateEqualsToday();

        for (PlannedTransaction pt : plannedTransactions) {
            new Thread(
                    () -> {
                        logInfo("Scheduler started..." + pt.getPtName());
                        try {
                            Thread.sleep(
                                    pt.getNextExecutionDate().toEpochSecond(ZoneOffset.UTC) * SEC_TO_MILIS
                                            -
                                            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * SEC_TO_MILIS);
                            logInfo("Scheduler executing planned transaction " + pt.getPtName());
                            plannedTransactionController.execute(pt);
                            logInfo(pt.getPtName() + " executed.");//TODO reschedule executed planned transaction
                        } catch (Exception e) {
                            logError(HttpStatus.INTERNAL_SERVER_ERROR, e);
                        }
                    }
            ).start();
        }
    }

    //-----------------------< /Scheduled Task >----------------------//


}
