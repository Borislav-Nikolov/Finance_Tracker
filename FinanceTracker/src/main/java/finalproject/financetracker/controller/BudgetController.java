package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.repositories.BudgetRepository;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/profile", produces = "application/json")
public class BudgetController extends AbstractController {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;
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
    public BudgetInfoDTO createBudget(@RequestBody BudgetCreationDTO budgetCreationDTO, HttpSession session,
                                      HttpServletRequest request)
                                    throws IOException, MyException {
        budgetCreationDTO.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        String budgetName = budgetCreationDTO.getBudgetName();
        double amount = budgetCreationDTO.getAmount();
        LocalDate startingDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getStartingDate());
        LocalDate endDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getEndDate());
        long userId = user.getUserId();
        long categoryId = budgetCreationDTO.getCategoryId();
        this.validateDates(startingDate, endDate);
        Budget budget = new Budget(budgetName, amount, startingDate, endDate, userId, categoryId);
        budgetRepository.save(budget);
        return getBudgetInfoDTO(budget);
    }

    @PutMapping(value = "/budgets/{budgetId}")
    public BudgetInfoDTO editBudget(
            @PathVariable String budgetId,
            @RequestParam(value = "budgetName", required = false) String budgetName,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            HttpSession session,
            HttpServletRequest request)
                                throws IOException, MyException {
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
            this.validateDates(budget.getStartingDate(), timeUtil.checkParseLocalDate(endDate));
            budget.setEndDate(timeUtil.checkParseLocalDate(endDate));
        }
        if (categoryId != null) {
            budget.setCategoryId(parsedLong);
        }
        budgetRepository.save(budget);
        return getBudgetInfoDTO(budget);
    }

    @DeleteMapping(value = "/budgets/{budgetId}")
    public BudgetInfoDTO deleteBudget(@PathVariable String budgetId, HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        this.validateBudgetOwnership(budget, user.getUserId());
        budgetRepository.delete(budget);
        return this.getBudgetInfoDTO(budget);
    }

    public void subtractFromBudgets(double amount, long userId, long categoryId) throws SQLException {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == categoryId && !budget.getEndDate().isAfter(LocalDate.now())) {
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

    private void validateDates(LocalDate startingDate, LocalDate endDate) throws InvalidRequestDataException {
        if (!startingDate.isBefore(endDate) || startingDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestDataException("Incorrect date input.");
        }
    }

    private void validateBudgetOwnership(Budget budget, long sessionUserId) throws NotFoundException {
        if (budget == null || budget.getUserId() != sessionUserId) {
            throw new NotFoundException("Budget not found.");
        }
    }
}
