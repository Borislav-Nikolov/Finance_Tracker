package finalproject.financetracker.exceptions.budget_exceptions;

public class BudgetDatesException extends BudgetException {
    public BudgetDatesException() {
        super("Budget starting date is not before ending date.");
    }
}
