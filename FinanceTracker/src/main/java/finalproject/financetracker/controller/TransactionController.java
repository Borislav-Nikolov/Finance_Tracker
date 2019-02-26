package finalproject.financetracker.controller;

import finalproject.financetracker.model.dtos.account.AddTransactionRespDTO;
import finalproject.financetracker.model.pojos.Account;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.TransactionDao;
import finalproject.financetracker.model.daos.UserRepository;
import finalproject.financetracker.model.exceptions.ForbiddenRequestException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.NotFoundException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RequestMapping(value = "/profile/transactions")
@Controller
public class TransactionController extends AbstractController {

    private final TransactionDao dao;
    private final UserRepository userRepo;
    private final AccountController accountController;
    private final CategoryController categoryController;

    @Autowired
    TransactionController(TransactionDao dao,
                          UserRepository userRepo,
                          AccountController accountController,
                          CategoryController categoryController){

        this.dao = dao;
        this.userRepo = userRepo;
        this.accountController =accountController;
        this.categoryController = categoryController;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public AddTransactionRespDTO addTransaction(@RequestBody Transaction t,
                                      HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        if (isNotValidTransaction(t)) {
            throw new InvalidRequestDataException();
        }
        User u = getLoggedUserWithIdFromSession(sess);

        if (!(u.getUserId() == t.getUserId())) {
            //todo change msg
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        List<Transaction> transactions = dao.getAllTByUserId(t.getUserId());

        for (Transaction transaction : transactions) {
            if (t.getTransactionName().equalsIgnoreCase(transaction.getTransactionName())) {
                throw new ForbiddenRequestException("transaction with such name exists");
            }
        }
        User user =userRepo.getByUserId(t.getUserId());
        Category category = categoryController.getCategoryById(t.getCategoryId(),sess);
        Account account = accountController.getAccByIdLong(t.getUserId(),sess);
        t = dao.add(t);
        AddTransactionRespDTO returnDto = new AddTransactionRespDTO(
                t.getTransactionId(),
                t.getTransactionName(),
                t.getAmount(),
                t.getExecutionDate(),
                t.getUserId(),
                t.getCategoryId(),
                t.getAccountId(),
                user.getUsername(),
                account.getAccountName(),
                category.getCategoryName());
        return returnDto;
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public AddTransactionRespDTO updateTransaction(@RequestBody Transaction transaction,
                                         HttpSession sess)
            throws NotLoggedInException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            IOException,
            NotFoundException,
            SQLException {

        if (transaction.getTransactionId()<=0){
            throw new InvalidRequestDataException("invalid transaction ID");
        }
        return addTransaction(transaction,sess);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteTransaction(@RequestBody Transaction t,
                                  HttpSession sess)
            throws InvalidRequestDataException,
                   NotLoggedInException,
                   IOException,
                   NotFoundException {

        if (t.getTransactionId()<= 0 || isNotValidTransaction(t)) {
            throw new InvalidRequestDataException();
        }
        User u = getLoggedUserWithIdFromSession(sess);

        if (!(u.getUserId() == t.getUserId())) {
            //todo change msg
            throw new NotLoggedInException("not logged in a.userId!=u.userId");
        }
        List<Transaction> transactions = dao.getAllTByUserId(t.getUserId());

        for (Transaction transaction : transactions) {
            if (t.getTransactionId() == (transaction.getTransactionId())) {
                dao.deleteT(t.getTransactionId());
            }
        }
        throw new NotFoundException("transaction not found, delete unsuccessful");
    }
}
