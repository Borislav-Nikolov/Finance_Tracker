package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.daos.AbstractDao;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.daos.TransactionDao;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.transaction.AddTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.UpdateTransactionDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.repositories.AccountRepo;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(value = "/profile", produces = "application/json")
@Controller
@ResponseBody
public class TransactionController extends AbstractController {


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
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(), sess, request);    // WebService
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess, request);        // WebService

        for (Transaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
                throw new ForbiddenRequestException("transaction with such name exists");
            }
        }

        Transaction t = new Transaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                LocalDateTime.now(),
                addTransactionDTO.getAccountId(),
                addTransactionDTO.getCategoryId());
        this.calculateBudgetAndAccountAmount(t, u.getUserId());
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

        AbstractDao.SQLColumnName columnName = AbstractDao.SQLColumnName.EXECUTION_DATE;
        if (order != null) {
            switch (order) {
                case "amount": {
                    columnName = AbstractDao.SQLColumnName.AMOUNT;
                    break;
                }
                case "tname": {
                    columnName = AbstractDao.SQLColumnName.TRANSACTION_NAME;
                    break;
                }
                case "aname": {
                    columnName = AbstractDao.SQLColumnName.ACCOUNT_NAME;
                    break;
                }
            }
        }
        AbstractDao.SQLOderBy orderBy = AbstractDao.SQLOderBy.ASC;
        if (desc != null) {
            if (desc.equalsIgnoreCase("true")) orderBy = AbstractDao.SQLOderBy.DESC;
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

    void calculateBudgetAndAccountAmount(Transaction t, long userId) throws SQLException {
        double transactionAmount = 0;
        Account a = accountDao.getById(t.getAccountId());
        Category c = categoryRepository.findByCategoryId(t.getCategoryId());

        if (c.isIncome()) {
            transactionAmount = t.getAmount();
        } else {
            transactionAmount = t.getAmount() * -1;
                budgetController.subtractFromBudgets(t.getAmount(), userId, c.getCategoryId());

            t.setAmount(Math.abs(transactionAmount));
        }
        a.setAmount(a.getAmount() + transactionAmount);
        accountRepo.save(a);
    }
}
