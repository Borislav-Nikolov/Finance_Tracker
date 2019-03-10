package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CategoryDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private CategoryRepository categoryRepository;

    public Category addCategory(Category category) {
        categoryRepository.save(category);
        return category;
    }

    public void  deleteCategory(Category category) {
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
