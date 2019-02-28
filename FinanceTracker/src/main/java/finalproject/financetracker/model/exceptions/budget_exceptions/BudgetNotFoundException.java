package finalproject.financetracker.model.exceptions.budget_exceptions;

public class BudgetNotFoundException extends BudgetException {
    public BudgetNotFoundException() {
        super("Budget does not exist.");
    }
}
