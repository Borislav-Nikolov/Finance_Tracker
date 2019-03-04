package finalproject.financetracker.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalproject.financetracker.model.daos.PlannedTransactionDao;
import finalproject.financetracker.model.daos.PlannedTransactionRepo;
import finalproject.financetracker.model.daos.TransactionRepo;
import finalproject.financetracker.model.dtos.account.AddAccountDTO;
import finalproject.financetracker.model.dtos.account.EditAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.account.ReturnUserBalanceDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping(value = "/profile", produces = "application/json")
@ResponseBody
public class AccountController extends AbstractController{
    public static final int SEC_TO_MILIS = 1000;
    private final AccountDao dao;
    private final TransactionRepo tRepo;
    private final PlannedTransactionRepo ptRepo;
    private final PlannedTransactionDao ptDao;

    @Autowired
    AccountController(AccountDao dao,
                      TransactionRepo tRepo,
                      PlannedTransactionRepo ptRepo,
                      PlannedTransactionDao ptDao) {
        this.tRepo = tRepo;
        this.dao = dao;
        this.ptRepo = ptRepo;
        this.ptDao = ptDao;
    }

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
        checkIfAccountWithSuchNameExists(checkAcc,a.getAccountName());
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
            InvalidRequestDataException {

        long idL = checkValidStringId(accId);
        return getAccByIdLong(idL,sess);
    }

    ReturnAccountDTO getAccByIdLong( long accId,
                                       HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException{

        User u = getLoggedValidUserFromSession(sess);
        Account account = dao.getById(accId);
        checkIfNotNull(Account.class,account);
        checkIfBelongsToLoggedUserAndReturnUser(account.getUserId(),u);
        List<Transaction> transactions = tRepo.findAllByAccountId(accId);
        List<PlannedTransaction> plannedTransactions = ptRepo.findAllByAccountId(accId);
        return new ReturnAccountDTO(account)
                .withUser(u)
                .withTransactions(transactions)
                .withPlannedTransactions(plannedTransactions);
    }

    //--------------delete account---------------------//
    @RequestMapping(
            value = "/accounts/{accId}",
            method = RequestMethod.DELETE)
    public ReturnAccountDTO deleteAcc(@PathVariable(name = "accId") String accId,
                          HttpSession sess)
            throws
            SQLException,
            IOException,
            NotLoggedInException,
            NotFoundException,
            InvalidRequestDataException {

        ReturnAccountDTO a = getAccById(accId,sess);  //   "/accounts/{accId}  Web Service
        dao.deleteAcc(AccountDao.SQLColumnName.ACCOUNT_ID, AccountDao.SQLCompareOperator.EQUALS, a.getAccountId());   // WHERE account_id = accId
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
        ReturnAccountDTO returnAccountDTO = getAccByIdLong(a.getAccountId(),sess);
        Account[] allUserAccounts = dao.getAllAccountsAsc(u.getUserId());
        checkIfAccountWithSuchNameAndDiffIdExists(allUserAccounts,a.getAccountName(),a.getAccountId());
        dao.updateAcc(a);
        returnAccountDTO.setAccountName(a.getAccountName());
        return returnAccountDTO.withUser(u);

    }

    //--------------show all accounts for a given userId ascending/descending---------------------//
    @RequestMapping(
            value = "/accounts",
            method = RequestMethod.GET)
    public Account[] allAccOrdered(@RequestParam(value = "desc", required = false) boolean order,
                                   HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            SQLException{

        User u = getLoggedValidUserFromSession(sess);

        if (order) {
            return dao.getAllAccountsDesc(u.getUserId());
        }else {
            return dao.getAllAccountsAsc(u.getUserId());
        }
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

    @Scheduled(cron = "0 0 * * * *")
    private void executePlannedTransactions(){
//        List<PlannedTransaction> plannedTransactions = ptDao.getAllWhereExecDateEqualsToday();
//        for (PlannedTransaction pt : p    slannedTransactions){
//            new Thread(
//                    ()->{
//                        try {
//                            Thread.sleep(
//                                    pt.getNextExecutionDate().toEpochSecond(ZoneOffset.UTC)* SEC_TO_MILIS
//                                    -
//                                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*SEC_TO_MILIS);
//                            this.tRepo.save(
//                                    new Transaction(
//                                            pt.getPtName().concat("_planned-").concat(LocalDate.now().toString()),
//                                            pt.getPtAmount(),
//                                            LocalDateTime.now(),
//                                            pt.getAccountId(),
//                                            pt.getUserId(),
//                                            pt.getCategoryId()));
//                        } catch (InterruptedException e) {
//                            super.logError(HttpStatus.INTERNAL_SERVER_ERROR,e); e.printStackTrace();
//                        }
//                    }
//            ).start();
//        }
    }
}
