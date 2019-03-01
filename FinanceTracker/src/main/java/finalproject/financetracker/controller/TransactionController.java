package finalproject.financetracker.controller;

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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@RequestMapping(value = "/profile")
@Controller
public class TransactionController extends AbstractController {

    private final TransactionRepo repo;
    private final UserRepository userRepo;
    private final AccountController accountController;
    private final CategoryController categoryController;

    @Autowired
    TransactionController(TransactionRepo repo,
                          UserRepository userRepo,
                          AccountController accountController,
                          CategoryController categoryController) {

        this.repo = repo;
        this.userRepo = userRepo;
        this.accountController = accountController;
        this.categoryController = categoryController;
    }

    protected boolean checkIfValidTransaction(Transaction t) {
        return t == null ||
                t.getTransactionName() == null ||
                t.getTransactionName().isEmpty() ||
                (t.getAmount() <= 0) ||
                t.getUserId() <= 0 ||
                t.getCategoryId() <= 0 ||
                t.getExecutionDate() == null;
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
        List<ITransaction> transactions = repo.findAllByUserId(u.getUserId());

        for (ITransaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
                throw new ForbiddenRequestException("transaction with such name exists");
            }
        }
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);  // WebSercvice
        ReturnAccountDTO a = accountController.getAccById(addTransactionDTO.getAccountId(),sess);
        Transaction t = new Transaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                new Date(),
                addTransactionDTO.getAccountId(),
                u.getUserId(),
                addTransactionDTO.getCategoryId());
        t = repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUsername(u.getUsername())
                .withCategoryName(c.getCategoryName())
                .withAccountName(a.getAccountName());
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ReturnTransactionDTO getTransactionById(@PathVariable(value = "id") long id,
                                                   HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        checkValidId(id);
        User u = getLoggedValidUserFromSession(sess);
        Transaction t = repo.getOne(id);
        checkIfNotNull(t);
        checkIfBelongsToLoggedUser(t.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccById(t.getAccountId(), sess);
        return new ReturnTransactionDTO(t)
                .withUsername(u.getUsername())
                .withCategoryName(c.getCategoryName())
                .withAccountName(a.getAccountName());
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

        transactionDTO.checkValid();
        User u = getLoggedValidUserFromSession(sess);
        Transaction t = repo.getOne(transactionDTO.getTransactionId());
        checkIfNotNull(t);
        checkIfBelongsToLoggedUser(t.getUserId(), u);
        Category c = categoryController.getCategoryById(t.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccById(t.getAccountId(),sess);
        t = repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUsername(u.getUsername())
                .withAccountName(a.getAccountName())
                .withCategoryName(c.getCategoryName());
    }

    @RequestMapping(value = "/transactions/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnTransactionDTO deleteTransaction(@PathVariable(value = "id") long deleteId,
                                                  HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            SQLException,
            NotFoundException {

        checkValidId(deleteId);
        ReturnTransactionDTO t = getTransactionById(deleteId, sess);
        repo.deleteByTransactionId(t.getTransactionId());
        return t;
    }


}
