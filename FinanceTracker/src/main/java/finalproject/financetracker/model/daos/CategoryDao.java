package finalproject.financetracker.model.daos;

import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.model.pojos.Budget;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.PlannedTransaction;
import finalproject.financetracker.model.pojos.Transaction;
import finalproject.financetracker.model.repositories.BudgetRepository;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.repositories.PlannedTransactionRepo;
import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CategoryDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private PlannedTransactionRepo plannedTransactionRepo;
    @Autowired
    private BudgetRepository budgetRepository;

    public Category addCategory(Category category) {
        categoryRepository.save(category);
        return category;
    }

    @Transactional(rollbackFor = MyException.class)
    public void  deleteCategory(Category category) {
        long defaultCategoryId = category.isIncome() ? 1 : 2;
        List<Transaction> transactions = transactionRepo.findAllByCategoryId(category.getCategoryId());
        for (Transaction transaction : transactions) {
            transaction.setCategoryId(defaultCategoryId);
            transactionRepo.save(transaction);
        }
        List<PlannedTransaction> plannedTransactions = plannedTransactionRepo.findAllByCategoryId(category.getCategoryId());
        for (PlannedTransaction plannedTransaction : plannedTransactions) {
            plannedTransaction.setCategoryId(defaultCategoryId);
            plannedTransactionRepo.save(plannedTransaction);
        }
        List<Budget> budgets = budgetRepository.findAllByCategoryId(defaultCategoryId);
        for (Budget budget : budgets) {
            budget.setCategoryId(defaultCategoryId);
            budgetRepository.save(budget);
        }
        categoryRepository.delete(category);
    }

    public Category getCategoryByNameAndUserId(String categoryName, long userId) {
        return categoryRepository.findByCategoryNameAndUserId(categoryName, userId);
    }

    public List<Category> getPredefinedAndUserCategories(long userId) {
        List<Category> categories = getCategoriesByUserId(userId);
        categories.addAll(getAllPredefinedCategories());
        return categories;
    }

    public Category getCategoryById(long categoryId) {
        return categoryRepository.findByCategoryId(categoryId);
    }

    public List<Category> getCategoriesByUserId(long userId) {
        return categoryRepository.findAllByUserId(userId);
    }

    public List<Category> getAllPredefinedCategories() {
        List<Map<String, Object>> predefinedList =
                jdbcTemplate.queryForList("SELECT * FROM final_project.categories WHERE user_id IS NULL;");
        List<Category> categories = new ArrayList<>();
        for (Map<String, Object> categoryMap : predefinedList) {
            int categoryId = (int) categoryMap.get("category_id");
            Category category = categoryRepository.findByCategoryId((long) categoryId);
            categories.add(category);
        }
        return categories;
    }

}
