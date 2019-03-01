package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
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

    public void addAllPredefinedCategories() {
        addCategory(new Category("Clothes", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("shirt_icon.png").getImageId()));
        addCategory(new Category("Kids", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("child_icon.png").getImageId()));
        addCategory(new Category("Toys", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("toy_icon.png").getImageId()));
        addCategory(new Category("Work", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("computer_icon.png").getImageId()));
        addCategory(new Category("Electronics", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("smartphone_icon.png").getImageId()));
        addCategory(new Category("Pets", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("pawprint_icon.png").getImageId()));
        addCategory(new Category("Child support", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("child_icon.png").getImageId()));
        addCategory(new Category("Scholarship", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("book_icon.png").getImageId()));
        addCategory(new Category("Bonus", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("laptop_icon.png").getImageId()));
        addCategory(new Category("Gift", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("baloons_icon.png").getImageId()));
        addCategory(new Category("Investment income", true, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("watch_icon.png").getImageId()));
        addCategory(new Category("Entertainment", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("controller_icon.png").getImageId()));
        addCategory(new Category("Car", false, UserDao.DEFAULT_CATEGORY_USER_ID,
                imageDao.getImageByFileName("car_icon.png").getImageId()));
    }

}
