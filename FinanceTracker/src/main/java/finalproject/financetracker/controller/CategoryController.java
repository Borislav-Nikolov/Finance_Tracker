package finalproject.financetracker.controller;

import finalproject.financetracker.model.exceptions.ForbiddenRequestException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.CategoryDao;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
public class CategoryController extends AbstractController {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private CategoryDao categoryDao;

    @GetMapping(value = "/categories/createCategory")
    public Category createCategory(@RequestParam("categoryName") String categoryName,
                                   @RequestParam("isIncome") boolean isIncome,
                                   @RequestParam("imageId") long imageId,
                                   HttpSession session,
                                   HttpServletResponse resp)
                        throws Exception {
        if (UserController.isLoggedIn(session)) {
            if (categoryName == null || categoryName.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().append("Please, input category name.");
                resp.sendRedirect("/categories");
                return null;
            }
            User user = userDao.getUserByUsername(session.getAttribute("Username").toString());
            long userId = userDao.getUserId(user);
            Image image = imageDao.getImageById(imageId);
            List<Category> categories = categoryDao.getPredefinedAndUserCategories(userId);
            if (!isRepeating(categories, categoryName)) {
                Category newCategory = new Category(categoryName, isIncome, userId, imageId, image);
                return categoryDao.addCategory(newCategory);
            }
        } else {
            throw new NotLoggedInException();
        }
        throw new CategoryAlreadyExistsException();
    }

    private boolean isRepeating(List<Category> categories, String newCategoryName) {
        for (Category category : categories) {
            if (category.getCategoryName().equals(newCategoryName)) return true;
        }
        return false;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    private class CategoryAlreadyExistsException extends MyException {
        CategoryAlreadyExistsException() {
            super("Category by that name already exists.");
        }
    }

    public Category getCategoryById(long categoryId, HttpSession session) throws IOException, NotLoggedInException, InvalidRequestDataException, ForbiddenRequestException {
        Category category = categoryDao.getCategoryById(categoryId);
        User user = getLoggedUserWithIdFromSession(session);
        if (category.getUserId() == user.getUserId()) {
            return category;
        }
        throw new ForbiddenRequestException("Category does not belong to this user.");
    }
}
