package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
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

    public Category addCategory(Category category) {
        String sql = "INSERT INTO final_project.categories(category_name, is_income, user_id, image_id) " +
                "VALUES (?, ?, ?, ?);";
        String categoryName = category.getCategoryName();
        boolean isIncome = category.isIncome();
        long userId = category.getUserId();
        long imageId = category.getImage().getImageId();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryName);
            ps.setBoolean(2, isIncome);
            ps.setLong(3, userId);
            ps.setLong(4, imageId);
            return ps;
        }, keyHolder);

        category.setCategoryId(keyHolder.getKey().longValue());
        return category;
    }

    public void  deleteCategory(Category category) {
        String sql = "DELETE FROM final_project.categories WHERE category_id = ?;";
        long categoryId = category.getCategoryId();
        // TODO check at mapping deletion method if exists to avoid error if repeated tries with URL
        jdbcTemplate.update(sql, categoryId);
    }


    public Category getCategoryByNameAndUserId(String categoryName, long userId) {
        String sql = "SELECT * FROM final_project.categories WHERE category_name LIKE ? AND user_id = ?;";
        Category category;
        try {
            category = jdbcTemplate.queryForObject(sql,
                    new Object[] { categoryName, userId }, new BeanPropertyRowMapper<>(Category.class));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
        return category;
    }
    public List<Category> getCategoriesByUser(User user) {
        String sql = "SELECT * FROM final_project.categories WHERE user_id = ?;";
        long userId = userDao.getUserId(user);
        List<Category> categories = new ArrayList<>();
        List<Map<String, Object>> rows = null;
        try {
            rows = jdbcTemplate.queryForList(sql, userId);
        } catch (DataAccessException ex) {
            System.out.println(ex.getMessage());
        }
        if (rows == null) {
            rows = jdbcTemplate.queryForList(sql, 0);
        } else {
            rows.addAll(jdbcTemplate.queryForList(sql, 0));
        }
        for (Map<String, Object> row : rows) {
            Category category = new Category();
            category.setCategoryId((long) row.get("category_id"));
            category.setCategoryName((String) row.get("category_name"));
            category.setIncome((boolean) row.get("is_income"));
            category.setUserId((long) row.get("user_id"));
            long imageId = (long) row.get("image_id");
            category.setImage(imageDao.getImageById(imageId));
            categories.add(category);
        }
        return categories;
    }

    public void addAllPredefined() {
        addCategory(new Category("Clothes", false, 0,
                imageDao.getImageByFileName("shirt_icon.png")));
        addCategory(new Category("Kids", false, 0,
                imageDao.getImageByFileName("child_icon.png")));
        addCategory(new Category("Toys", false, 0,
                imageDao.getImageByFileName("toy_icon.png")));
        addCategory(new Category("Work", true, 0,
                imageDao.getImageByFileName("computer_icon.png")));
        addCategory(new Category("Electronics", false, 0,
                imageDao.getImageByFileName("smartphone_icon.png")));
        addCategory(new Category("Pets", false, 0,
                imageDao.getImageByFileName("pawprint_icon.png")));
        addCategory(new Category("Child support", true, 0,
                imageDao.getImageByFileName("child_icon.png")));
        addCategory(new Category("Scholarship", true, 0,
                imageDao.getImageByFileName("book_icon.png")));
        addCategory(new Category("Bonus", true, 0,
                imageDao.getImageByFileName("laptop_icon.png")));
        addCategory(new Category("Gift", true, 0,
                imageDao.getImageByFileName("baloons_icon.png")));
        addCategory(new Category("Investment income", true, 0,
                imageDao.getImageByFileName("watch_icon.png")));
        addCategory(new Category("Entertainment", false, 0,
                imageDao.getImageByFileName("controller_icon.png")));
        addCategory(new Category("Car", false, 0,
                imageDao.getImageByFileName("car_icon.png")));
    }

}
