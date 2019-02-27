package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
