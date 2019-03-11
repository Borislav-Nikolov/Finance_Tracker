package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.dtos.MsgObjectDTO;
import finalproject.financetracker.model.pojos.*;
import finalproject.financetracker.model.repositories.BudgetRepository;
import finalproject.financetracker.model.repositories.UserRepository;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetCreationDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetInfoDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetsViewDTO;
import finalproject.financetracker.exceptions.MyException;
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
    public MsgObjectDTO createBudget(@RequestBody BudgetCreationDTO budgetCreationDTO, HttpSession session,
                                     HttpServletRequest request)
                                    throws IOException, MyException {
        budgetCreationDTO.checkValid();
        User user = this.getLoggedValidUserFromSession(session, request);
        String budgetName = budgetCreationDTO.getBudgetName();
        double amount = budgetCreationDTO.getAmount();
        if (amount < 0) {
            throw new InvalidRequestDataException("Budget amount is less than 0.");
        }
        LocalDate startingDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getStartingDate());
        LocalDate endDate = timeUtil.checkParseLocalDate(budgetCreationDTO.getEndDate());
        long userId = user.getUserId();
        long categoryId = budgetCreationDTO.getCategoryId();
        Category category = categoryController.getCategoryById(categoryId, session, request);
        if (category.isIncome()) {
            throw new InvalidRequestDataException("Budget category cannot be of type income.");
        }
        categoryController.validateCategoryAndUserOwnership(user, category);
        Budget budget = new Budget(budgetName, amount, startingDate, endDate, userId, categoryId);
        this.validateDates(budget);
        budgetRepository.save(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget created successfully.", LocalDateTime.now(), budgetInfo);
    }

    @PutMapping(value = "/budgets/{budgetId}")
    public MsgObjectDTO editBudget(
            @PathVariable String budgetId,
            @RequestParam(value = "budgetName", required = false) String budgetName,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "startingDate", required = false) String startingDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            HttpSession session,
            HttpServletRequest request)
                                throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Long parsedCategoryId = null;
        if (categoryId != null) {
            parsedCategoryId = parseLong(categoryId);
            Category category = categoryController.getCategoryById(parsedCategoryId, session, request);
            if (category.isIncome()) {
                throw new InvalidRequestDataException("Budget category cannot be of type income.");
            }
        }
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        if (amount != null) {
            Double parsedAmount = parseDouble(amount);
            if (parsedAmount < 0) {
                throw new InvalidRequestDataException("Budget amount is less than 0.");
            }
            budget.setAmount(parsedAmount);
        }
        this.validateBudgetOwnership(budget, user.getUserId());
        if (budgetName != null) {
            budget.setBudgetName(budgetName);
        }
        this.changeDates(budget, startingDate, endDate);
        if (categoryId != null) {
            budget.setCategoryId(parsedCategoryId);
        }
        budgetRepository.save(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget edited successfully.", LocalDateTime.now(), budgetInfo);
    }

    @DeleteMapping(value = "/budgets/{budgetId}")
    public MsgObjectDTO deleteBudget(@PathVariable String budgetId, HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Budget budget = budgetRepository.findByBudgetId(parseLong(budgetId));
        this.validateBudgetOwnership(budget, user.getUserId());
        budgetRepository.delete(budget);
        BudgetInfoDTO budgetInfo = getBudgetInfoDTO(budget);
        return new MsgObjectDTO("Budget deleted successfully.", LocalDateTime.now(), budgetInfo);
    }

    void subtractFromBudgets(double amount, long userId, long categoryId) throws SQLException {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == categoryId && budget.getEndDate().isAfter(LocalDate.now())) {
                subtractFromASingleBudget(budget, userId, amount);
            }
        }
    }
    private void subtractFromASingleBudget(Budget budget, long userId, double amount) throws SQLException {
        budget.setAmount(budget.getAmount() - amount);
        User user = userRepository.getByUserId(userId);
        if (budget.getAmount() <= 50 && userDao.isEligibleForReceivingEmail(user.getEmail())) {
            emailSender.sendBudgetNearLimitEmail(user, budget);
        }
        budgetRepository.save(budget);
    }

    private BudgetInfoDTO getBudgetInfoDTO(Budget budget) {
        return new BudgetInfoDTO(
                budget.getBudgetId(), budget.getBudgetName(),
                budget.getAmount(), budget.getStartingDate(),
                budget.getEndDate(), budget.getUserId(), budget.getCategoryId());
    }

    private void changeDates(Budget budget, String startingDate, String endDate) throws InvalidRequestDataException {
        if (startingDate != null) {
            LocalDate parsedStartingDate = timeUtil.checkParseLocalDate(startingDate);
            budget.setStartingDate(parsedStartingDate);
        }
        if (endDate != null) {
            LocalDate parsedEndDate = timeUtil.checkParseLocalDate(endDate);
            budget.setEndDate(parsedEndDate);
        }
        this.validateDates(budget);
    }

    /* ----- VALIDATIONS ----- */

    private void validateDates(Budget budget)
            throws InvalidRequestDataException {
        if (!budget.getStartingDate().isBefore(budget.getEndDate()) || budget.getStartingDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestDataException("Incorrect date input.");
        }
    }

    private void validateBudgetOwnership(Budget budget, long sessionUserId) throws NotFoundException {
        if (budget == null || budget.getUserId() != sessionUserId) {
            throw new NotFoundException("Budget not found.");
        }
    }
}
