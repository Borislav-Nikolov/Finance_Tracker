package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.daos.AbstractDao;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.daos.TransactionDao;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.transaction.AddTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.UpdateTransactionDTO;
import finalproject.financetracker.model.pojos.*;
import finalproject.financetracker.model.repositories.AccountRepo;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RequestMapping(value = "/profile", produces = "application/json")
@Controller
@ResponseBody
public class TransactionController extends AbstractController {
    @Autowired
    private PlannedTransactionController plannedTransactionController;
    @Autowired
    private TransactionRepo repo;
    @Autowired
    private TransactionDao dao;
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private AccountController accountController;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private CategoryController categoryController;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BudgetController budgetController;

    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public ReturnTransactionDTO addTransaction(@RequestBody AddTransactionDTO addTransactionDTO,
                                               HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        addTransactionDTO.checkValid();
        List<Transaction> transactions = repo.findAllByAccountId(addTransactionDTO.getAccountId());
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(), sess, request);// WebService
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess, request); // WebService

//        for (Transaction transaction : transactions) {
//            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
//                throw new ForbiddenRequestException("transaction with such name exists");
//            }
//        }

        Transaction t = new Transaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                LocalDateTime.now(),
                addTransactionDTO.getAccountId(),
                addTransactionDTO.getCategoryId());
        this.calculateBudgetAndAccountAmount(t);
        repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/transactions/{transactionId}", method = RequestMethod.GET)
    public ReturnTransactionDTO getTransactionById(@PathVariable(value = "transactionId") String transactionId,
                                                   HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        Transaction t = validateDataAndGetByIdFromRepo(transactionId, repo, Transaction.class);
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(), sess, request);
        checkIfBelongsToLoggedUser(a.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess, request);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }


    //--------------get all transaction for given user by filter---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public List<ReturnTransactionDTO> getAllWhere(@RequestParam(value = "acc", required = false) String accId,
                                                  @RequestParam(value = "cat", required = false) String catId,
                                                  @RequestParam(value = "from", required = false) String startDate,
                                                  @RequestParam(value = "to", required = false) String endDate,
                                                  @RequestParam(value = "income", required = false) String income,
                                                  @RequestParam(value = "order", required = false) String order,
                                                  @RequestParam(value = "desc", required = false) String desc,
                                                  @RequestParam(value = "limit", required = false) String limit,
                                                  @RequestParam(value = "offset", required = false) String offset,
                                                  HttpSession session, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(session, request);
        Integer limitInt = null;
        if (limit != null && !limit.isEmpty()) {
            limitInt = parseInt(limit);
            if (limitInt <=0) limitInt = null;
        }
        if (limitInt == null){
            limitInt = AbstractDao.QUERY_RETURN_LIMIT_DEFAULT;
        }

        Integer offsetInt = null;
        if (offset != null && !offset.isEmpty()) {
            offsetInt = parseInt(offset);
            if (offsetInt <=0) offsetInt = null;
        }
        if (offsetInt == null){
            offsetInt = AbstractDao.QUERY_RETURN_OFFSET_DEFAULT;
        }

        Long accIdLong = null;
        if (accId != null && !accId.isEmpty()) {
            accIdLong = parseLong(accId);
            ReturnAccountDTO a = accountController.getAccByIdLong(accIdLong, session, request);
        }
        Category c = null;
        Long catIdLong = null;
        if (catId != null && !catId.isEmpty()) {
            catIdLong = parseLong(catId);
            c = categoryController.getCategoryById(catIdLong, session, request);
        }
        Long startDateMillis = (startDate != null) ? parseLong(startDate) : 0L;
        Long endDateMillis = (endDate != null) ? parseLong(endDate) : System.currentTimeMillis();

        Boolean isIncome = null;
        if (income != null && !income.isEmpty()) {
            if (income.equalsIgnoreCase("true")) isIncome = true;
            if (income.equalsIgnoreCase("false")) isIncome = false;
            if (c != null) {
                isIncome = c.isIncome();
            }
        }

        AbstractDao.SQLOrderBy columnName = AbstractDao.SQLOrderBy.EXECUTION_DATE;
        if (order != null) {
            switch (order) {
                case "amount": {
                    columnName = AbstractDao.SQLOrderBy.AMOUNT;
                    break;
                }
                case "tname": {
                    columnName = AbstractDao.SQLOrderBy.TRANSACTION_NAME;
                    break;
                }
                case "aname": {
                    columnName = AbstractDao.SQLOrderBy.ACCOUNT_NAME;
                    break;
                }
            }
        }
        AbstractDao.SQLOrder orderBy = AbstractDao.SQLOrder.ASC;
        if (desc != null) {
            if (desc.equalsIgnoreCase("true")) orderBy = AbstractDao.SQLOrder.DESC;
        }
        return dao.getAllByAccIdStartDateEndDateIsIncome(
                u.getUserId(),
                accIdLong,
                catIdLong,
                startDateMillis,
                endDateMillis,
                isIncome,
                columnName,
                orderBy,
                limitInt,
                offsetInt);
    }

    //-------------- edit transaction ---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.PUT)
    public ReturnTransactionDTO updateTransaction(@RequestBody UpdateTransactionDTO transactionDTO,
                                                  HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        transactionDTO.checkValid();
        Transaction t = validateDataAndGetByIdFromRepo(transactionDTO.getTransactionId(), repo, Transaction.class);
        t.setTransactionName(transactionDTO.getTransactionName());
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(), sess, request);
        checkIfBelongsToLoggedUser(a.getUserId(),u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess, request);
        repo.saveAndFlush(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withAccount(a)
                .withCategory(c);
    }

    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public ReturnTransactionDTO deleteTransaction(@PathVariable(value = "id") String deleteId,
                                                  HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        ReturnTransactionDTO t = getTransactionById(deleteId, sess, request);
        repo.deleteByTransactionId(t.getTransactionId());
        return t;
    }

    void calculateBudgetAndAccountAmount(Transaction t) throws SQLException, MyException {
        double transactionAmount = 0;
        Account a = validateDataAndGetByIdFromRepo(t.getAccountId(),accountRepo,Account.class);
        Category c = validateDataAndGetByIdFromRepo(t.getCategoryId(),categoryRepository,Category.class);

        if (c.isIncome()) {
            transactionAmount = t.getAmount();
        } else {
            transactionAmount = t.getAmount() * -1;
                budgetController.subtractFromBudgets(t.getAmount(), a.getUserId(), c.getCategoryId());

            t.setAmount(Math.abs(transactionAmount));
        }
        a.setAmount(a.getAmount() + transactionAmount);
        accountRepo.save(a);
    }

    @Async //start the code in a separate Thread
    @Transactional(rollbackFor = Exception.class)
    void execute(PlannedTransaction pt){
        boolean error = false;
        while (pt.getNextExecutionDate().isBefore(LocalDateTime.now())) {
            try {
                synchronized (PlannedTransactionController.concurrentLock) {
                    plannedTransactionController.recalculateAndSave(pt);
                }
            } catch (Exception e) {
                logError(HttpStatus.INTERNAL_SERVER_ERROR, e);
                error = true;
                break;
            }
        }
        try {
            if (!error && pt.getNextExecutionDate()
                    .isBefore(LocalDate.now().plusDays(1).atTime(0, 0, 0))) {
                logInfo("Waiting..... " + pt);
                Thread.sleep(pt.getNextExecutionDate()
                        .toEpochSecond(ZoneOffset.UTC) * SEC_TO_MILLIS
                        -
                        LocalDateTime.now()
                                .toEpochSecond(ZoneOffset.UTC) * SEC_TO_MILLIS);
                logInfo("Executing... " + pt);
                synchronized (PlannedTransactionController.concurrentLock) {
                    plannedTransactionController.recalculateAndSave(pt);
                }
                logInfo("Executed.... " + pt);
            }
        } catch (Exception e) {
            logError(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
