package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return categoryRepository.findAllByUserId(1);
    }

    // TODO fix this (constructor needs more parameters + adjust Admin ID)
//    public void addAllPredefined() {
//        addCategory(new Category("Clothes", false, 0,
//                imageDao.getImageByFileName("shirt_icon.png")));
//        addCategory(new Category("Kids", false, -1,
//                imageDao.getImageByFileName("child_icon.png")));
//        addCategory(new Category("Toys", false, 0,
//                imageDao.getImageByFileName("toy_icon.png")));
//        addCategory(new Category("Work", true, 0,
//                imageDao.getImageByFileName("computer_icon.png")));
//        addCategory(new Category("Electronics", false, 0,
//                imageDao.getImageByFileName("smartphone_icon.png")));
//        addCategory(new Category("Pets", false, 0,
//                imageDao.getImageByFileName("pawprint_icon.png")));
//        addCategory(new Category("Child support", true, 0,
//                imageDao.getImageByFileName("child_icon.png")));
//        addCategory(new Category("Scholarship", true, 0,
//                imageDao.getImageByFileName("book_icon.png")));
//        addCategory(new Category("Bonus", true, 0,
//                imageDao.getImageByFileName("laptop_icon.png")));
//        addCategory(new Category("Gift", true, 0,
//                imageDao.getImageByFileName("baloons_icon.png")));
//        addCategory(new Category("Investment income", true, 0,
//                imageDao.getImageByFileName("watch_icon.png")));
//        addCategory(new Category("Entertainment", false, 0,
//                imageDao.getImageByFileName("controller_icon.png")));
//        addCategory(new Category("Car", false, 0,
//                imageDao.getImageByFileName("car_icon.png")));
//    }

}
