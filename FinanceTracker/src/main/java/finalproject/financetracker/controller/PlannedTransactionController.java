package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.daos.AccountDao;
import finalproject.financetracker.model.dtos.account.ReturnAccountDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.AddPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.ReturnPlannedTransactionDTO;
import finalproject.financetracker.model.dtos.plannedTransaction.UpdatePlannedTransactionDTO;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.PlannedTransactionRepo;
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
public class PlannedTransactionController extends AbstractController {

    @Autowired
    private PlannedTransactionRepo repo;
    @Autowired
    private AccountController accountController;
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private CategoryController categoryController;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TransactionController transactionController;
    @Autowired
    private TransactionRepo transactionRepo;


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
        Category c = categoryController.getCategoryById(addTransactionDTO.getCategoryId(), sess);  // WebSercvice
        ReturnAccountDTO a = accountController.getAccByIdLong(addTransactionDTO.getAccountId(), sess);

        for (PlannedTransaction transaction : transactions) {
            if (addTransactionDTO.getTransactionName().equalsIgnoreCase(transaction.getPtName())) {
                throw new ForbiddenRequestException("planned transaction with such name exists");
            }
        }
        PlannedTransaction t = new PlannedTransaction(
                addTransactionDTO.getTransactionName(),
                addTransactionDTO.getAmount(),
                LocalDateTime.now().plusSeconds(addTransactionDTO.getRepeatPeriod() / 1000),  // nextExecutionDate LocalDateTime.now() + repeatPeriod
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
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(id, repo, PlannedTransaction.class);
        checkIfBelongsToLoggedUser(pt.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(), sess);
        return new ReturnPlannedTransactionDTO(pt)
                .withUser(u)
                .withCategory(c)
                .withAccount(a);
    }

    //--------------get all transactions for given user---------------------//
    @RequestMapping(value = "/ptransactions", method = RequestMethod.GET)
    @ResponseBody
    public List<ReturnPlannedTransactionDTO> getAllPlannedTransaction(
            @RequestParam(value = "accId", required = false) String accId,
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
        PlannedTransaction pt = validateDataAndGetByIdFromRepo(transactionDTO.getTransactionId(), repo, PlannedTransaction.class);
        pt.setPtName(transactionDTO.getTransactionName());
        checkIfBelongsToLoggedUser(pt.getUserId(), u);
        Category c = categoryController.getCategoryById(pt.getCategoryId(), sess);
        ReturnAccountDTO a = accountController.getAccByIdLong(pt.getAccountId(), sess);
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

    @Transactional(rollbackFor = Exception.class)
    public void execute(PlannedTransaction pt) throws SQLException {
        Transaction t = new Transaction(
                pt.getPtName().concat("_planned-").concat(LocalDateTime.now().toString()),
                pt.getPtAmount(),
                LocalDateTime.now(),
                pt.getAccountId(),
                pt.getUserId(),
                pt.getCategoryId());
        t = transactionController.calculateBudgetAndAccountAmount(t); //TODO implement logic for not enough money
        this.transactionRepo.save(t);                                 // to finish the plannedTransaction
    }                                                                 // (reschedule/abort/negative account amount)

    List<ReturnPlannedTransactionDTO> listEntitiesToListDTOs(List<PlannedTransaction> list, User u) {
        return list.stream().map((PlannedTransaction t) -> {
            try {
                return new ReturnPlannedTransactionDTO(t)
                        .withUser(u)
                        .withCategory(categoryRepository.findByCategoryId(t.getCategoryId()))
                        .withAccount(accountDao.getById(t.getAccountId()));
            } catch (SQLException e) {
                logError(HttpStatus.INTERNAL_SERVER_ERROR, e);
                throw new ServerErrorException();
            }
        }).collect(Collectors.toList());
    }
}
