package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.dtos.MsgObjectDTO;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.repositories.BudgetRepository;
import finalproject.financetracker.model.repositories.TransactionRepo;
import finalproject.financetracker.model.repositories.UserRepository;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetCreationDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetInfoDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetsViewDTO;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.pojos.Budget;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.utils.TimeUtil;
import finalproject.financetracker.utils.emailing.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/profile", produces = "application/json")
public class BudgetController extends AbstractController {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private TimeUtil timeUtil;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CategoryController categoryController;

    @GetMapping(value = "/budgets")
    public BudgetsViewDTO viewBudgets(HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        List<Budget> budgets = budgetRepository.findAllByUserId(user.getUserId());
        List<BudgetInfoDTO> budgetsInfo = new ArrayList<>();
        for (Budget budget : budgets) {
            BudgetInfoDTO budgetInfoDTO = new BudgetInfoDTO(
                    budget.getBudgetId(), budget.getBudgetName(),
                    budget.getAmount(), budget.getStartingDate(),
                    budget.getEndDate(), budget.getUserId(), budget.getCategoryId());
            budgetsInfo.add(budgetInfoDTO);
        }
        return new BudgetsViewDTO(budgetsInfo);
    }

    @GetMapping(value = "/budgets/{budgetId}")
    public BudgetInfoDTO viewBudget(@PathVariable String budgetId, HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        this.validateBudgetOwnership(budget, user.getUserId());
        return this.getBudgetInfoDTO(budget);
    }

    @PostMapping(value = "/budgets")
    public MsgObjectDTO createBudget(@RequestBody BudgetCreationDTO budgetCreationDTO, HttpSession session,
                                     HttpServletRequest request)
                                    throws IOException, MyException, SQLException {
        budgetCreationDTO.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        String budgetName = budgetCreationDTO.getBudgetName();
        double amount = budgetCreationDTO.getAmount();
        LocalDate startingDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getStartingDate());
        LocalDate endDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getEndDate());
        long userId = user.getUserId();
        long categoryId = budgetCreationDTO.getCategoryId();
        Budget budget = new Budget(budgetName, amount, startingDate, endDate, userId, categoryId);
        this.validateDates(budget);
        budgetRepository.save(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget created successfully.", new Date(), budgetInfo);
    }

    @PutMapping(value = "/budgets/{budgetId}")
    public MsgObjectDTO editBudget(
            @PathVariable String budgetId,
            @RequestParam(value = "budgetName", required = false) String budgetName,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            HttpSession session,
            HttpServletRequest request)
                                throws IOException, MyException, SQLException {
        Double parsedAmount = parseDouble(amount);
        Long parsedLong = parseLong(categoryId);
        User user = this.getLoggedValidUserFromSession(session, request);
        categoryController.getCategoryById(parsedLong, session, request);
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        this.validateBudgetOwnership(budget, user.getUserId());
        if (budgetName != null) {
            budget.setBudgetName(budgetName);
        }
        if (amount != null) {
            budget.setAmount(parsedAmount);
        }
        if (endDate != null) {
            LocalDate parsedEndDate = timeUtil.checkParseLocalDate(endDate);
            budget.setEndDate(parsedEndDate);
            this.validateDates(budget);
        }
        if (categoryId != null) {
            budget.setCategoryId(parsedLong);
        }
        budgetRepository.save(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget edited successfully.", new Date(), budgetInfo);
    }

    @DeleteMapping(value = "/budgets/{budgetId}")
    public MsgObjectDTO deleteBudget(@PathVariable String budgetId, HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        this.validateBudgetOwnership(budget, user.getUserId());
        budgetRepository.delete(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget deleted successfully.", new Date(), budgetInfo);
    }

    public void subtractFromBudgets(double amount, long userId, long categoryId) throws SQLException {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == categoryId && budget.getEndDate().isAfter(LocalDate.now())) {
                budget.setAmount(budget.getAmount() - amount);
                User user = userRepository.getByUserId(userId);
                if (budget.getAmount() <= 50 && userDao.isEligibleForReceivingEmail(user.getEmail())) {
                    emailSender.sendBudgetNearLimitEmail(user, budget);
                }
                budgetRepository.save(budget);
            }
        }
    }

    private BudgetInfoDTO getBudgetInfoDTO(Budget budget) {
        return new BudgetInfoDTO(
                budget.getBudgetId(), budget.getBudgetName(),
                budget.getAmount(), budget.getStartingDate(),
                budget.getEndDate(), budget.getUserId(), budget.getCategoryId());
    }

    /* ----- VALIDATIONS ----- */

    private void validateDates(Budget budget) throws InvalidRequestDataException, SQLException {
        if (!budget.getStartingDate().isBefore(budget.getEndDate())) {
            throw new InvalidRequestDataException("Incorrect date input.");
        }
        if (budget.getStartingDate().isBefore(LocalDate.now())) {
            this.recalculateBudget(budget);
        }
    }
    // TODO will produce untrue results if repeated
    private void recalculateBudget(Budget budget) throws SQLException {
        // TODO get needed with query at TransactionDao
        List<Transaction> transactions = transactionRepo.findAllByUserId(budget.getUserId());
        double totalSum = 0;
        LocalDate startingDate = budget.getStartingDate();
        for (Transaction transaction : transactions) {
            LocalDateTime execDate = transaction.getExecutionDate();
            LocalDateTime startingDateTime = startingDate.atTime(0, 0, 0, 0);
            if (execDate.isAfter(startingDateTime)) {
                totalSum += transaction.getAmount();
            }
        }
        subtractFromBudgets(totalSum, budget.getUserId(), budget.getCategoryId());
    }
    private void validateBudgetOwnership(Budget budget, long sessionUserId) throws NotFoundException {
        if (budget == null || budget.getUserId() != sessionUserId) {
            throw new NotFoundException("Budget not found.");
        }
    }
}
