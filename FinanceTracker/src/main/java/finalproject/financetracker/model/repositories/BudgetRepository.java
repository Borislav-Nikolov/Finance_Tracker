package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUserId(long userId);
    Budget findByBudgetNameAndUserId(String budgetName, long userId);
    Budget findByBudgetId(long budgetId);
    List<Budget> findAllByCategoryId(Long categoryId);
}
