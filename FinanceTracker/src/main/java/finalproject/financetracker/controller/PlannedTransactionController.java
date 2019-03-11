package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.ForbiddenRequestException;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.daos.AbstractDao;
import finalproject.financetracker.model.daos.PlannedTransactionDao;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.AddPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.UpdatePlannedTransactionDTO;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.repositories.PlannedTransactionRepo;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@RequestMapping(value = "/profile", produces = "application/json")
@Controller
public class PlannedTransactionController extends AbstractController {
    static final ReentrantLock concurrentLock = new ReentrantLock();
    @Autowired
    private PlannedTransactionRepo repo;
    @Autowired
    private PlannedTransactionDao dao;
    @Autowired
    private AccountController accountController;
    @Autowired
    private CategoryController categoryController;
    @Autowired
    private TransactionController transactionController;
    @Autowired
    private TransactionRepo transactionRepo;


    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnPlannedTransactionDTO addPlannedTransaction(@RequestBody AddPlannedTransactionDTO addTransactionDTO,
                                                             HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        addTransactionDTO.checkValid();
        List<PlannedTransaction> transactions = repo.findAllByAccountId(addTransactionDTO.getAccountId());
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess, request);// WebService
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(), sess, request);

        for (PlannedTransaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getPtName())) {
                throw new ForbiddenRequestException("planned transaction with such name exists");
            }
        }
        PlannedTransaction t = new PlannedTransaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                LocalDateTime.now(),
                addTransactionDTO.getAccountId(),
                addTransactionDTO.getCategoryId(),
                addTransactionDTO.getRepeatPeriod());
        reSchedule(t,addTransactionDTO.getExecutionOffset());
        transactionController.execute(t);
        return new ReturnPlannedTransactionDTO(repo.save(t))
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/ptransactions/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ReturnPlannedTransactionDTO getPlannedTransactionById(@PathVariable(value = "id") String id,
                                                                 HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(id, repo, PlannedTransaction.class);
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(), sess, request);
        checkIfBelongsToLoggedUser(a.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess, request);

        return new ReturnPlannedTransactionDTO(pt)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //--------------get all transactions for given user---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.GET)
    @ResponseBody
    public List<ReturnPlannedTransactionDTO> getAllPlannedTransaction(
            @RequestParam(value = "acc", required = false) String accId,
            @RequestParam(value = "cat", required = false) String catId,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "desc", required = false) String desc,
            @RequestParam(value = "income", required = false) String income,
            HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        User u = getLoggedValidUserFromSession(sess, request);
        Long accIdL = null;
        if (accId != null && !accId.isEmpty()) {
            accIdL = parseLong(accId);
            accountController.getAccByIdLong(accIdL, sess, request);
        }
        Category c = null;
        Long catIdL = null;
        if (catId != null && !catId.isEmpty()) {
            catIdL = parseLong(catId);
            c = categoryController.getCategoryById(catIdL, sess, request);
        }
        Boolean isIncome = null;
        if (income != null && !income.isEmpty()) {
            if (income.equalsIgnoreCase("true")) isIncome = true;
            if (income.equalsIgnoreCase("false")) isIncome = false;
            if (c != null) {
                isIncome = c.isIncome();
            }
        }
        AbstractDao.SQLOrderBy columnName = AbstractDao.SQLOrderBy.NEXT_EXECUTION_DATE;
        if (order != null) {
            switch (order) {
                case "amount": {
                    columnName = AbstractDao.SQLOrderBy.AMOUNT;
                    break;
                }
                case "ptname": {
                    columnName = AbstractDao.SQLOrderBy.PT_NAME;
                    break;
                }
                case "aname": {
                    columnName = AbstractDao.SQLOrderBy.ACCOUNT_NAME;
                    break;
                }
                case "cname": {
                    columnName = AbstractDao.SQLOrderBy.CATEGORY_NAME;
                    break;
                }
            }
        }
        AbstractDao.SQLOrder orderBy = AbstractDao.SQLOrder.ASC;
        if (desc != null) {
            if (desc.equalsIgnoreCase("true")) orderBy = AbstractDao.SQLOrder.DESC;
        }
        return dao.getAllByAccIdIsIncomeOrder(
                u.getUserId(),
                accIdL,
                catIdL,
                isIncome,
                columnName,
                orderBy);
    }

    //-------------- edit transaction ---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnPlannedTransactionDTO updatePlannedTransaction(
            @RequestBody UpdatePlannedTransactionDTO transactionDTO,
            HttpSession sess,
            HttpServletRequest request)

            throws
            IOException,
            SQLException,
            MyException {

        transactionDTO.checkValid();
        User u = getLoggedValidUserFromSession(sess, request);
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(
                transactionDTO.getTransactionId(),
                repo,
                PlannedTransaction.class
        );
        pt.setPtName(transactionDTO.getTransactionName());
        if (transactionDTO.getRepeatPeriod() > pt.getRepeatPeriod()) {
            pt.setNextExecutionDate(
                    pt.getNextExecutionDate()
                            .plusSeconds(
                                    (transactionDTO.getRepeatPeriod() - pt.getRepeatPeriod()) / SEC_TO_MILLIS));
        } else {
            pt.setNextExecutionDate(
                    pt.getNextExecutionDate()
                            .minusSeconds((pt.getRepeatPeriod() - transactionDTO.getRepeatPeriod()) / SEC_TO_MILLIS));
        }
        pt.setRepeatPeriod(transactionDTO.getRepeatPeriod());
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(), sess, request);
        checkIfBelongsToLoggedUser(a.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess, request);

        pt = repo.saveAndFlush(pt);
        return new ReturnPlannedTransactionDTO(pt)
                .withUser(u)
                .withAccount(a)
                .withCategory(c);
    }

    @RequestMapping(value = "/ptransactions/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @Transactional
    public ReturnPlannedTransactionDTO deletePlannedTransaction(@PathVariable(value = "id") String deleteId,
                                                                HttpSession sess, HttpServletRequest request)
            throws
            IOException,
            SQLException,
            MyException {

        ReturnPlannedTransactionDTO t = getPlannedTransactionById(deleteId, sess, request);
        repo.deleteByPtId(t.getTransactionId());
        return t;
    }

    @Transactional(rollbackFor = Exception.class)
    public void recalculateAndSave(PlannedTransaction pt) throws SQLException, MyException {
        Transaction t = new Transaction(
                pt.getPtName()
                        .concat(pt.getNextExecutionDate()
                                .format(DateTimeFormatter.ofPattern("(dd.MM.YY/HH:mm)"))),
                pt.getPtAmount(),
                LocalDateTime.now(),
                pt.getAccountId(),
                pt.getCategoryId());
        transactionController.calculateBudgetAndAccountAmount(t);
        this.transactionRepo.save(t);
        reSchedule(pt,0);
        repo.save(pt);
    }

    private void reSchedule(PlannedTransaction pt, long init) {
        long period = init > 0 ? init : pt.getRepeatPeriod();
        if (pt.getRepeatPeriod()% MILLIS_FOR_MONTH != 0) {
            pt.setNextExecutionDate(pt.getNextExecutionDate().plusSeconds(period / SEC_TO_MILLIS));
        }else {
            pt.setNextExecutionDate(pt.getNextExecutionDate().plusMonths(period/MILLIS_FOR_MONTH));
        }
    }

    void startScheduledCheck(LocalDate toDate) {
        new PlannedTransactionScheduler(toDate).start();
    }

    public class PlannedTransactionScheduler extends Thread {
        private LocalDate localDate;

        public PlannedTransactionScheduler(LocalDate localDate){
            this.localDate = localDate;
        }

        @Override
        public void run() {
            logInfo("PlannedTransactionScheduler is performing check...");
            List<PlannedTransaction> transactions = PlannedTransactionController.this.dao
                    .getAllPlannedTransactionBefore(localDate);
            logInfo("Found " + transactions.size() + " transactions for execution.");

            for (PlannedTransaction pt : transactions) {
                transactionController.execute(pt);
            }
        }
    }
}
