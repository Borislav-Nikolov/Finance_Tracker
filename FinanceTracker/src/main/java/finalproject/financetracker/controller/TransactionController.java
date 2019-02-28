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
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@RequestMapping(value = "/profile/transactions")
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
        return  t == null ||
                t.getTransactionName() == null ||
                t.getTransactionName().isEmpty() ||
                (t.getAmount() <= 0) ||
                t.getUserId() <= 0 ||
                t.getCategoryId() <= 0 ||
                t.getExecutionDate()==null;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ReturnTransactionDTO addTransaction(@RequestBody AddTransactionDTO addTransactionDTO,
                                                HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        addTransactionDTO.checkValid();
        User u = getLoggedValidUserFromSession(sess);
        List<Transaction> transactions = repo.findAllByUserId(addTransactionDTO.getUserId());

        for (Transaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
                throw new ForbiddenRequestException("transaction with such name exists");
            }
        }
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccById(addTransactionDTO.getUserId(), sess);
        Transaction t = new Transaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                new Date(),
                u.getUserId(),
                addTransactionDTO.getCategoryId());
        t = repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUsername(u.getUsername())
                .withCategoryName(c.getCategoryName());
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnTransactionDTO updateTransaction(@RequestBody UpdateTransactionDTO transactionDTO,
                                                   HttpSession sess)
            throws
            InvalidRequestDataException, NotLoggedInException, IOException, NotFoundException, SQLException, ForbiddenRequestException {

        transactionDTO.checkValid();
        User u = getLoggedValidUserFromSession(sess);
        Transaction t = new Transaction(transactionDTO.getTransactionId(),
                transactionDTO.getTransactionName(),
                transactionDTO.getAmount(),
                new Date(),
                u.getUserId(),
                transactionDTO.getCategoryId());
        Category c = categoryController.getCategoryById(t.getCategoryId(),sess);
        repo.save(t);
        return new ReturnTransactionDTO(t)
                .withUsername(u.getUsername())
                .withCategoryName(c.getCategoryName());
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteTransaction(@RequestParam long deleteId,
                                  HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException{

        User u = getLoggedValidUserFromSession(sess);
        Transaction t = repo.getOne(deleteId);
        checkIfBelongsToLoggedUser(t.getUserId(),u);
        repo.deleteByTransactionId(t.getTransactionId());
    }
}
