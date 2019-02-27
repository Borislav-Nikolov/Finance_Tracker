package finalproject.financetracker.controller;

import finalproject.financetracker.model.dtos.account.CommonMsgDTO;
import finalproject.financetracker.model.dtos.budgetDTOs.BudgetsViewDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
public class BudgetController extends AbstractController {
    @GetMapping(value = "/budgets")
    public BudgetsViewDTO viewBudgets(HttpSession session) {
        // TODO
        return new BudgetsViewDTO();
    }

    @GetMapping(value = "/budgets/createBudget")
    public CommonMsgDTO createBudget(HttpSession session) {
        // TODO
        return new CommonMsgDTO("", new Date());
    }

    @DeleteMapping(value = "budgets/deleteBudget")
    public CommonMsgDTO deleteBudget(HttpSession session) {
        // TODO
        return new CommonMsgDTO("", new Date());
    }
}
