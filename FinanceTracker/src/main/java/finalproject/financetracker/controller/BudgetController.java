package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.BudgetRepository;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetCreationDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetInfoDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetsViewDTO;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import finalproject.financetracker.model.exceptions.budget_exceptions.BudgetDatesException;
import finalproject.financetracker.model.exceptions.budget_exceptions.BudgetNotFoundException;
import finalproject.financetracker.model.pojos.Budget;
import finalproject.financetracker.model.pojos.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/profile", produces = "application/json")
public class BudgetController extends AbstractController {

    @Autowired
    private BudgetRepository budgetRepository;


    @GetMapping(value = "/budgets")
    public BudgetsViewDTO viewBudgets(HttpSession session) throws IOException, MyException {
        User user = this.getLoggedUserWithIdFromSession(session);
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
    public BudgetInfoDTO viewBudget(@PathVariable long budgetId, HttpSession session) throws IOException, MyException {
        User user = this.getLoggedUserWithIdFromSession(session);
        Budget budget = budgetRepository.findByBudgetId(budgetId);
        this.validateBudgetOwnership(budget, user.getUserId());
        return this.getBudgetInfoDTO(budget);
    }

    @PostMapping(value = "/budgets/createBudget")
    public BudgetInfoDTO createBudget(@RequestBody BudgetCreationDTO budgetCreationDTO, HttpSession session)
                                    throws IOException, MyException {
        User user = this.getLoggedUserWithIdFromSession(session);
        String budgetName = budgetCreationDTO.getBudgetName();
        double amount = budgetCreationDTO.getAmount();
        LocalDate startingDate = budgetCreationDTO.getStartingDate();
        LocalDate endDate = budgetCreationDTO.getEndDate();
        long userId = user.getUserId();
        long categoryId = budgetCreationDTO.getCategoryId();
        this.validateDates(startingDate, endDate);
        Budget budget = new Budget(budgetName, amount, startingDate, endDate, userId, categoryId);
        budgetRepository.save(budget);
        budget = budgetRepository.findByBudgetNameAndUserId(budgetName, userId);
        return new BudgetInfoDTO(
                budget.getBudgetId(), budgetName, amount,
                startingDate, endDate, userId, categoryId);
    }

    @PutMapping(value = "/budgets/{budgetId}/edit")
    public BudgetInfoDTO editBudget(
            @PathVariable long budgetId,
            @RequestParam(value = "budgetName", required = false) String budgetName,
            @RequestParam(value = "amount", required = false) Double amount,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            HttpSession session)
                                throws IOException, MyException {
        User user = this.getLoggedUserWithIdFromSession(session);
        Budget budget = budgetRepository.findByBudgetId(budgetId);
        this.validateBudgetOwnership(budget, user.getUserId());
        this.validateDates(budget.getStartingDate(), endDate);
        if (budgetName != null) {
            budget.setBudgetName(budgetName);
        }
        if (amount != null) {
            budget.setAmount(amount);
        }
        if (endDate != null) {
            budget.setEndDate(endDate);
        }
        if (categoryId != null) {
            budget.setCategoryId(categoryId);
        }
        budgetRepository.save(budget);
        return getBudgetInfoDTO(budget);
    }

    @DeleteMapping(value = "/budgets/deleteBudget/{budgetId}")
    public BudgetInfoDTO deleteBudget(@PathVariable long budgetId, HttpSession session) throws IOException, MyException {
        User user = this.getLoggedUserWithIdFromSession(session);
        Budget budget = budgetRepository.findByBudgetId(budgetId);
        this.validateBudgetOwnership(budget, user.getUserId());
        budgetRepository.delete(budget);
        return this.getBudgetInfoDTO(budget);
    }

    public void subtractFromBudgets(double amount, long userId, long categoryId) {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == categoryId) {
                budget.setAmount(budget.getAmount() - amount);
                // TODO send email / message if budget is near 0
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

    private void validateDates(LocalDate startingDate, LocalDate endDate) throws BudgetDatesException {
        if (!startingDate.isBefore(endDate) || startingDate.isBefore(LocalDate.now())) {
            throw new BudgetDatesException();
        }
    }

    private void validateBudgetOwnership(Budget budget, long sessionUserId) throws MyException {
        if (budget == null){
            throw new BudgetNotFoundException();
        } else if (budget.getUserId() != sessionUserId) {
            throw new NotLoggedInException("User does not match budget.");
        }
    }

    /* ----- TESTING ----- */
//    @GetMapping(value = "testDate")
//    public dto1 testDate() {
//        return new dto1(LocalDate.now());
//    }
//    @AllArgsConstructor
//    @Getter
//    @Setter
//    static class dto1 {
//        LocalDate ld;
//    }
//    @GetMapping(value = "testString")
//    public dto2 testString() {
//        return new dto2("test test");
//    }
//    @AllArgsConstructor
//    @Getter
//    @Setter
//    static class dto2 {
//        String string;
//    }
//    @GetMapping(value = "testInt")
//    public dto3 testInteger() {
//        return new dto3(new Integer(5));
//    }
//    @AllArgsConstructor
//    @Getter
//    @Setter
//    static class dto3 {
//        Integer integer;
//    }
    /* ----- /TESTING ----- */
}
