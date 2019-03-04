package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.daos.TransactionRepo;
import finalproject.financetracker.model.daos.UserRepository;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.transaction.AddTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.ReturnTransactionDTO;
import finalproject.financetracker.model.dtos.transaction.UpdateTransactionDTO;
import finalproject.financetracker.model.exceptions.ForbiddenRequestException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.NotFoundException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import finalproject.financetracker.model.pojos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(value = "/profile")
@Controller
public class TransactionController extends AbstractController {

    private final TransactionRepo repo;
    private final AccountDao accountDao;
    private final AccountController accountController;
    private final CategoryController categoryController;

    @Autowired
    TransactionController(TransactionRepo repo,
                          UserRepository userRepo,
                          AccountController accountController,
                          CategoryController categoryController,
                          AccountDao aDao) {

        this.repo = repo;
        this.accountDao = aDao;
        this.accountController = accountController;
        this.categoryController = categoryController;
    }

    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    @ResponseBody
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

        for (Transaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
                throw new ForbiddenRequestException("transaction with such name exists");
            }
        }
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);  // WebSercvice
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(),sess);
        double transactionAmount = 0;

        if (c.isIncome()){
            transactionAmount = addTransactionDTO.getAmount();
        }else {
           transactionAmount =
                    (addTransactionDTO.getAmount() > a.getAmount())  //check if account amount is enough to make the transaction,
                            ? a.getAmount()                         // if not transaction amount is set to account amount
                            : addTransactionDTO.getAmount();
           transactionAmount = transactionAmount*-1;
        }
        accountDao.updateAccAmount((a.getAmount()+transactionAmount),a.getAccountId());
        Transaction t = new Transaction(
                addTransactionDTO.getTransactionName(),
                Math.abs(transactionAmount),
                LocalDateTime.now(),
                addTransactionDTO.getAccountId(),
                u.getUserId(),
                addTransactionDTO.getCategoryId());
        t = repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ReturnTransactionDTO getTransactionById(@PathVariable(value = "id") String id,
                                                   HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        Transaction t = validateDataAndGetByIdFromRepo(id,repo,Transaction.class);
        checkIfBelongsToLoggedUserAndReturnUser(t.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(), sess);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }



    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    @ResponseBody
    public List<Transaction> getAllTransactions(
                                               HttpSession sess)
            throws
            NotLoggedInException,
            IOException {

        User u = getLoggedValidUserFromSession(sess);
        return repo.findAllByUserId(u.getUserId());
    }

    //-------------- edit transaction ---------------------//
    @RequestMapping(value = "/transactions", method = RequestMethod.PUT)
    @ResponseBody
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
        Transaction t = validateDataAndGetByIdFromRepo(transactionDTO.getTransactionId(),repo,Transaction.class);
        t.setTransactionName(transactionDTO.getTransactionName());
        checkIfBelongsToLoggedUserAndReturnUser(t.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(t.getAccountId(),sess);
        t = repo.saveAndFlush(t);
        return new ReturnTransactionDTO(t)
                .withUser(u)
                .withAccount(a)
                .withCategory(c);
    }

    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @Transactional
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

    public ReturnTransactionDTO executePlannedTransaction(PlannedTransaction pt,
                                                          HttpSession sess)
            throws NotFoundException,
            ForbiddenRequestException,
            NotLoggedInException,
            IOException,
            SQLException,
            InvalidRequestDataException {

        return this.addTransaction(
                new AddTransactionDTO(
                        pt.getPtName(),
                        pt.getPtAmount(),
                        pt.getCategoryId(),
                        pt.getAccountId()),
                sess
        );
    }
}
