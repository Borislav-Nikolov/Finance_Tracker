package finalproject.financetracker.controller;

import finalproject.financetracker.model.Category;
import finalproject.financetracker.model.Image;
import finalproject.financetracker.model.User;
import finalproject.financetracker.model.daos.CategoryDao;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.daos.UserDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
public class CategoryController {

    @GetMapping("/testFile")
    public void testFile() {
        CategoryDao.addAllPredefined();
    }

    // TODO add option for user chosen images
    @GetMapping(value = "/categories/createCategory")
    public Category createCategory(@RequestParam("categoryName") String categoryName,
                                   @RequestParam("isIncome") boolean isIncome,
                                   @RequestParam("imageId") long imageId,
                                   HttpSession session,
                                   HttpServletResponse resp)
                        throws IOException {
        if (UserController.isLoggedIn(session)) {
            if (categoryName == null || categoryName.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().append("Please, input category name.");
                resp.sendRedirect("/categories");
                return null;
            }
            User user = UserDao.getUserByUsername(session.getAttribute("Username").toString());
            long userId = UserDao.getUserId(user);
            Image image = ImageDao.getImageById(imageId);
            List<Category> categories = CategoryDao.getCategoriesByUser(user);
            if (!isRepeating(categories, categoryName)) {
                Category newCategory = new Category(categoryName, isIncome, userId, image);
                return CategoryDao.addCategory(newCategory);
            }
        } else {
            // TODO consider exception in similar cases
            resp.setStatus(401);
            resp.getWriter().append("You are not logged in.");
            resp.sendRedirect("/login.html");
            return null;
        }
        resp.setStatus(400);
        resp.getWriter().append("Category by that name already exists.");
        resp.sendRedirect("/categories");
        return null;
    }

    private boolean isRepeating(List<Category> categories, String newCategoryName) {
        for (Category category : categories) {
            if (category.getCategoryName().equals(newCategoryName)) return true;
        }
        return false;
    }
}
