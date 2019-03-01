package finalproject.financetracker.controller;

import finalproject.financetracker.model.dtos.CommonMsgDTO;
import finalproject.financetracker.model.dtos.categoryDTOs.*;
import finalproject.financetracker.model.exceptions.ForbiddenRequestException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryAlreadyExistsException;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryNotFoundException;
import finalproject.financetracker.model.exceptions.image_exceptions.ImageNotFoundException;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.CategoryDao;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.daos.UserDao;
import finalproject.financetracker.model.exceptions.MyException;
import finalproject.financetracker.model.exceptions.NotLoggedInException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class CategoryController extends AbstractController {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private CategoryDao categoryDao;

    @GetMapping(value = "/categories")
    public CategoriesViewDTO viewCategories(HttpSession session) throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(user.getUserId());
        CategoriesViewDTO categoriesViewDTO = new CategoriesViewDTO(new ArrayList<>());
        for (Category category : categories) {
            Image image = imageDao.getImageById(category.getImageId());
            CategoryInfoDTO categoryInfoDTO =
                    new CategoryInfoDTO(
                            category.getCategoryId(),
                            category.getCategoryName(),
                            category.isIncome(),
                            category.getUserId(),
                            category.getImageId(),
                            image
                    );
            categoriesViewDTO.getCategories().add(categoryInfoDTO);
        }
        return categoriesViewDTO;
    }

    @GetMapping(value = "/budgets/{categoryId}")
    public CategoryInfoDTO viewBudgets(@PathVariable long categoryId, HttpSession session)
            throws IOException, MyException {
        // TODO
        return null;
    }

    @GetMapping(value = "/categories/createCategory")
    public CommonMsgDTO createCategory(@RequestParam("categoryName") String categoryName,
                                       @RequestParam("isIncome") boolean isIncome,
                                       // TODO figure out how this'll be coming
                                       @RequestParam("imageId") long imageId,
                                       HttpSession session)
                        throws Exception {
        User user = this.getLoggedValidUserFromSession(session);
        this.validateCategoryName(user.getUserId(), categoryName);
        this.validateImage(imageId);
        Category category = new Category(categoryName, isIncome, user.getUserId(), imageId);
        categoryDao.addCategory(category);
        return new CommonMsgDTO(categoryName + " category created successfully.", new Date());
    }

    @DeleteMapping(value = "categories/deleteCategory")
    public CommonMsgDTO deleteCategory(@RequestParam("categoryName") String categoryName, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        Category category = categoryDao.getCategoryByNameAndUserId(categoryName, user.getUserId());
        if (category == null) {
            throw new CategoryNotFoundException();
        }
        categoryDao.deleteCategory(category);
        return new CommonMsgDTO(categoryName + " category deleted successfully.", new Date());
    }

    public Category getCategoryById(long categoryId, HttpSession session)
            throws IOException, NotLoggedInException, ForbiddenRequestException {
        Category category = categoryDao.getCategoryById(categoryId);
        User user = getLoggedValidUserFromSession(session);
        System.out.println(user);
        System.out.println(category);
        if (category.getUserId() == UserDao.DEFAULT_CATEGORY_USER_ID || user.getUserId() == category.getUserId()) {
            return category;
        }
        throw new ForbiddenRequestException("Category does not belong to this user.");
    }

    /* ----- VALIDATIONS ----- */
    private void validateCategoryName(long userId, String categoryName) throws CategoryAlreadyExistsException {
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(userId);
        for (Category category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                throw new CategoryAlreadyExistsException();
            }
        }
    }
    private void validateImage(long imageId) throws ImageNotFoundException {
        if (imageDao.getImageById(imageId) == null) {
            throw new ImageNotFoundException();
        }
    }
}
