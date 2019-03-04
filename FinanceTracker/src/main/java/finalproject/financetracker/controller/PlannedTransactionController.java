package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.PlannedTransactionRepo;
import finalproject.financetracker.model.daos.UserRepository;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.AddPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.UpdatePlannedTransactionDTO;
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
public class PlannedTransactionController extends AbstractController {

    private final PlannedTransactionRepo repo;
    private final UserRepository userRepo;
    private final AccountController accountController;
    private final CategoryController categoryController;

    @Autowired
    PlannedTransactionController(PlannedTransactionRepo repo,
                          UserRepository userRepo,
                          AccountController accountController,
                          CategoryController categoryController) {

        this.repo = repo;
        this.userRepo = userRepo;
        this.accountController = accountController;
        this.categoryController = categoryController;
    }

    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.POST)
    @ResponseBody
    public ReturnPlannedTransactionDTO addPlannedTransaction(@RequestBody AddPlannedTransactionDTO addTransactionDTO,
                                                      HttpSession sess)
            throws InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        addTransactionDTO.checkValid();
        List<PlannedTransaction> transactions = repo.findAllByAccountId(addTransactionDTO.getAccountId());

        for (PlannedTransaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getPtName())) {
                throw new ForbiddenRequestException("planned transaction with such name exists");
            }
        }
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);  // WebSercvice
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(),sess);
        PlannedTransaction t = new PlannedTransaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                LocalDateTime.now().plusSeconds(addTransactionDTO.getRepeatPeriod()/1000),  // nextExecutionDate LocalDateTime.now() + repeatPeriod
                addTransactionDTO.getAccountId(),
                u.getUserId(),
                addTransactionDTO.getCategoryId(),
                addTransactionDTO.getRepeatPeriod());
        t = repo.save(t);
        return new ReturnPlannedTransactionDTO(t)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //-------------- get transaction by transactionId ---------------------//
    @RequestMapping(value = "/ptransactions/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ReturnPlannedTransactionDTO getPlannedTransactionById(@PathVariable(value = "id") String id,
                                                                 HttpSession sess)
            throws
            InvalidRequestDataException,
            NotLoggedInException,
            IOException,
            ForbiddenRequestException,
            NotFoundException,
            SQLException {

        User u = getLoggedValidUserFromSession(sess);
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(id,repo,PlannedTransaction.class);
        checkIfBelongsToLoggedUser(pt.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(), sess);
        return new ReturnPlannedTransactionDTO(pt)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //--------------add transaction for given account---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.GET)
    @ResponseBody
    public List<PlannedTransaction> addPlannedTransaction(
            HttpSession sess)
            throws
            NotLoggedInException,
            IOException {

        User u = getLoggedValidUserFromSession(sess);
        List<PlannedTransaction> transactions = repo.findAllByUserId(u.getUserId());
        return transactions;
    }

    //-------------- edit transaction ---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnPlannedTransactionDTO updatePlannedTransaction(@RequestBody UpdatePlannedTransactionDTO transactionDTO,
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
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(transactionDTO.getTransactionId(),repo,PlannedTransaction.class);
        pt.setPtName(transactionDTO.getTransactionName());
        checkIfBelongsToLoggedUser(pt.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(),sess);
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
                                                                HttpSession sess)
            throws
            NotLoggedInException,
            IOException,
            InvalidRequestDataException,
            ForbiddenRequestException,
            SQLException,
            NotFoundException {

        ReturnPlannedTransactionDTO t = getPlannedTransactionById(deleteId, sess);
        repo.deleteByPtId(t.getTransactionId());
        return t;
    }
}
