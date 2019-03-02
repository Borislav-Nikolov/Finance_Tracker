package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.CategoryRepository;
import finalproject.financetracker.model.dtos.categoryDTOs.*;
import finalproject.financetracker.model.exceptions.ForbiddenRequestException;
import finalproject.financetracker.model.exceptions.InvalidRequestDataException;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryAlreadyExistsException;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryException;
import finalproject.financetracker.model.exceptions.category_exceptions.CategoryMismatchException;
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
import java.util.List;

@RestController
@RequestMapping(value = "/profile", produces = "application/json")
public class CategoryController extends AbstractController {

    @Autowired
    private ImageDao imageDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(value = "/testNull")
    public void testIt(HttpSession session) throws Exception {
        this.getCategoryById(3, session);
    }

    @GetMapping(value = "/categories")
    public CategoriesViewDTO viewCategories(HttpSession session) throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(user.getUserId());
        CategoriesViewDTO categoriesViewDTO = new CategoriesViewDTO(new ArrayList<>());
        for (Category category : categories) {
            CategoryInfoDTO categoryInfoDTO = this.getCategoryInfoDTO(category);
            categoriesViewDTO.getCategories().add(categoryInfoDTO);
        }
        return categoriesViewDTO;
    }

    @GetMapping(value = "/categories/{categoryId}")
    public CategoryInfoDTO viewCategory(@PathVariable long categoryId, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        Category category = categoryRepository.findByCategoryId(categoryId);
        this.validateCategoryAndUserOwnership(user, category);
        return this.getCategoryInfoDTO(category);
    }

    @PostMapping(value = "/categories")
    public CategoryInfoDTO createCategory(@RequestBody CategoryCreationDTO categoryCreationDTO, HttpSession session)
                                        throws IOException, MyException {
        categoryCreationDTO.checkValid();
        String categoryName = categoryCreationDTO.getCategoryName();
        boolean isIncome = categoryCreationDTO.isIncome();
        long imageId = categoryCreationDTO.getImageId();
        User user = this.getLoggedValidUserFromSession(session);
        this.validateCategoryName(user.getUserId(), categoryName);
        this.validateImage(imageId);
        Category category = new Category(categoryName, isIncome, user.getUserId(), imageId);
        // TODO remove after showing to Stan
        System.out.println("1 ------------------------ " + category.getCategoryId() + " ----------------------------- ");
        categoryDao.addCategory(category);
        System.out.println("2 ------------------------ " + category.getCategoryId() + " ----------------------------- ");
        return this.getCategoryInfoDTO(category);
    }

    @PutMapping(value = "/categories/{categoryId}")
    public CategoryInfoDTO editCategory(@PathVariable long categoryId,
                                        @RequestParam(value = "categoryName", required = false) String categoryName,
                                        @RequestParam(value = "imageId", required = false) Long imageId,
                                        HttpSession session)
                                throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        Category category = categoryRepository.findByCategoryId(categoryId);
        this.validateCategoryAndUserOwnership(user, category);
        if (categoryName != null) {
            category.setCategoryName(categoryName);
        }
        if (imageId != null) {
            category.setImageId(imageId);
        }
        return getCategoryInfoDTO(category);
    }

    @DeleteMapping(value = "/categories/{categoryId}")
    public CategoryInfoDTO deleteCategory(@PathVariable("categoryId") long categoryId, HttpSession session)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session);
        Category category = categoryRepository.findByCategoryId(categoryId);
        this.validateCategoryAndUserOwnership(user, category);
        categoryDao.deleteCategory(category);
        return this.getCategoryInfoDTO(category);
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

    private CategoryInfoDTO getCategoryInfoDTO(Category category) {
        Image image = imageDao.getImageById(category.getImageId());
        return new CategoryInfoDTO(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.isIncome(),
                        category.getUserId(),
                        category.getImageId(),
                        image.getUri()
                );
    }

    /* ----- VALIDATIONS ----- */
    private void validateCategoryName(long userId, String categoryName) throws MyException {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new InvalidRequestDataException("No category name input.");
        }
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(userId);
        for (Category category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                throw new CategoryAlreadyExistsException();
            }
        }
    }
    private void validateCategoryAndUserOwnership(User user, Category category) throws CategoryException {
        if (category == null) {
            throw new CategoryNotFoundException();
        } else if (user.getUserId() != category.getUserId()) {
            throw new CategoryMismatchException();
        }
    }
    private void validateImage(long imageId) throws ImageNotFoundException {
        if (imageDao.getImageById(imageId) == null) {
            throw new ImageNotFoundException();
        }
    }
}
