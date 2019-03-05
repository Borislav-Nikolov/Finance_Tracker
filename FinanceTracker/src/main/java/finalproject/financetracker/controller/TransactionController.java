package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.transaction.AddTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.UpdateTransactionDTO;
import finalproject.financetracker.model.pojos.*;
import finalproject.financetracker.model.repositories.BudgetRepository;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(value = "/profile")
@Controller
@Transactional(rollbackFor = Exception.class)
@ResponseBody
public class TransactionController extends AbstractController {

    @Autowired private TransactionRepo repo;
    @Autowired private AccountDao accountDao;
    @Autowired private AccountController accountController;
    @Autowired private CategoryController categoryController;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BudgetRepository budgetRepository;


    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ReturnTransactionDTO addTransaction(@RequestBody AddTransactionDTO addTransactionDTO,
                                               HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        addTransactionDTO.checkValid();
        List<Transaction> transactions = repo.findAllByAccountId(addTransactionDTO.getAccountId());
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(), sess);    // WebService
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);        // WebService

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
                u.getUserId(),
                addTransactionDTO.getCategoryId());
        t = this.calculateBudgetAndAccountAmount(t);
        t = repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/transactions/{transactionId}", method = RequestMethod.GET)
    public ReturnTransactionDTO getTransactionById(@PathVariable(value = "transactionId") String transactionId,
                                                   HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        Transaction t = validateDataAndGetByIdFromRepo(transactionId, repo, Transaction.class);
        checkIfBelongsToLoggedUser(t.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(), sess);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }


    //--------------get all transaction for given user / accId---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public List<ReturnTransactionDTO> getAllTransactions(@RequestParam(value = "accId", required = false) String accId,
                                                         HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            InvalidRequestDataException {

        User u = getLoggedValidUserFromSession(sess);
        if (accId != null) {
            long accIdLong = parseNumber(accId);
            return listEntitiesToListDTOs(repo.findAllByAccountIdAndUserId(accIdLong, u.getUserId()), u);
        }
        return listEntitiesToListDTOs(repo.findAllByUserId(u.getUserId()), u);
    }

    //-------------- edit transaction ---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.PUT)
    public ReturnTransactionDTO updateTransaction(@RequestBody UpdateTransactionDTO transactionDTO,
                                                  HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        transactionDTO.checkValid();
        Transaction t = validateDataAndGetByIdFromRepo(transactionDTO.getTransactionId(), repo, Transaction.class);
        t.setTransactionName(transactionDTO.getTransactionName());
        checkIfBelongsToLoggedUser(t.getUserId(), u);
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(), sess);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);

        t = repo.saveAndFlush(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withAccount(a)
                .withCategory(c);
    }

    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public ReturnTransactionDTO deleteTransaction(@PathVariable(value = "id") String deleteId,
                                                  HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            SQLException,
            NotFoundException {

        ReturnTransactionDTO t = getTransactionById(deleteId, sess);
        repo.deleteByTransactionId(t.getTransactionId());
        return t;
    }

    Transaction calculateBudgetAndAccountAmount(Transaction t) throws SQLException {
        double transactionAmount = 0;
        Account a = accountDao.getById(t.getAccountId());
        Category c = categoryRepository.findByCategoryId(t.getCategoryId());

        if (c.isIncome()) {
            transactionAmount = t.getAmount();
        } else {
            transactionAmount = t.getAmount() * -1;
            List<Budget> budgets = budgetRepository.findAllByCategoryId(c.getCategoryId());
            for (Budget budget : budgets) {
                budget.setAmount(budget.getAmount() + t.getAmount());
                budgetRepository.save(budget);
            }
            t.setAmount(Math.abs(transactionAmount));
        }
        accountDao.updateAccAmount((a.getAmount() + transactionAmount), a.getAccountId());
        return t;
    }

    List<ReturnTransactionDTO> listEntitiesToListDTOs(List<Transaction> list, User u) {
        return list.stream().map((Transaction t) -> {
            try {
                return new ReturnTransactionDTO(t)
                        .withUser(u)
                        .withCategory(categoryRepository.findByCategoryId(t.getCategoryId()))
                        .withAccount(accountDao.getById(t.getAccountId()));
            } catch (SQLException e) {
                logError(HttpStatus.INTERNAL_SERVER_ERROR, e);
                throw new ServerErrorException();   //
            }
        }).collect(Collectors.toList());
    }
}
