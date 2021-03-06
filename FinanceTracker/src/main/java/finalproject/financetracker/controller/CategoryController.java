package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.*;
import finalproject.financetracker.model.dtos.MsgObjectDTO;
import finalproject.financetracker.model.repositories.CategoryRepository;
import finalproject.financetracker.model.dtos.categoryDTOs.*;
import finalproject.financetracker.model.pojos.Category;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.pojos.User;
import finalproject.financetracker.model.daos.CategoryDao;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.daos.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
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

    @GetMapping(value = "/categories")
    public CategoriesViewDTO viewCategories(HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(user.getUserId());
        CategoriesViewDTO categoriesViewDTO = new CategoriesViewDTO(new ArrayList<>());
        for (Category category : categories) {
            CategoryInfoDTO categoryInfoDTO = this.getCategoryInfoDTO(category);
            categoriesViewDTO.getCategories().add(categoryInfoDTO);
        }
        return categoriesViewDTO;
    }

    @GetMapping(value = "/categories/{categoryId}")
    public CategoryInfoDTO viewCategory(@PathVariable(value = "categoryId") String categoryId,
                                        HttpSession session, HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Category category = categoryRepository.findByCategoryId(parseLong(categoryId));
        this.validateCategoryAndUserOwnership(user, category);
        return this.getCategoryInfoDTO(category);
    }

    @PostMapping(value = "/categories")
    public MsgObjectDTO createCategory(@RequestBody CategoryCreationDTO categoryCreationDTO, HttpSession session,
                                       HttpServletRequest request)
                                        throws IOException, MyException {
        categoryCreationDTO.checkValid();
        String categoryName = categoryCreationDTO.getCategoryName();
        boolean isIncome = categoryCreationDTO.isIncome();
        long imageId = categoryCreationDTO.getImageId();
        User user = this.getLoggedValidUserFromSession(session, request);
        this.validateCategoryName(user.getUserId(), categoryName);
        this.validateImage(imageId);
        Category category = new Category(categoryName, isIncome, user.getUserId(), imageId);
        categoryDao.addCategory(category);
        CategoryInfoDTO categoryInfo = getCategoryInfoDTO(category);
        return new MsgObjectDTO("Category created successfully.", LocalDateTime.now(), categoryInfo);
    }

    @PutMapping(value = "/categories/{categoryId}")
    public MsgObjectDTO editCategory(@PathVariable("categoryId") String categoryId,
                                     @RequestParam(value = "categoryName", required = false) String categoryName,
                                     @RequestParam(value = "imageId", required = false) String imageId,
                                     HttpSession session, HttpServletRequest request)
                                throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Category category = categoryRepository.findByCategoryId(parseLong(categoryId));
        this.validateCategoryAndUserOwnership(user, category);
        if (category.getUserId() == null) {
            throw new UnauthorizedAccessException("Default category cannot be edited.");
        }
        if (categoryName != null) {
            category.setCategoryName(categoryName);
        }
        if (imageId != null) {
            category.setImageId(parseLong(imageId));
        }
        categoryRepository.save(category);
        CategoryInfoDTO categoryInfo = getCategoryInfoDTO(category);
        return new MsgObjectDTO("Category edited successfully.", LocalDateTime.now(), categoryInfo);
    }

    @DeleteMapping(value = "/categories/{categoryId}")
    public MsgObjectDTO deleteCategory(@PathVariable("categoryId") String categoryId, HttpSession session,
                                          HttpServletRequest request)
            throws IOException, MyException {
        User user = this.getLoggedValidUserFromSession(session, request);
        Category category = categoryRepository.findByCategoryId(parseLong(categoryId));
        this.validateCategoryAndUserOwnership(user, category);
        if (category.getUserId() == null) {
            throw new UnauthorizedAccessException("Default category cannot be deleted.");
        }
        categoryDao.deleteCategory(category);
        CategoryInfoDTO categoryInfo = getCategoryInfoDTO(category);
        return new MsgObjectDTO("Category deleted successfully.", LocalDateTime.now(), categoryInfo);
    }

    Category getCategoryById(long categoryId, HttpSession session, HttpServletRequest request)
            throws IOException,
            MyException {

        Category category = validateDataAndGetByIdFromRepo(categoryId,categoryRepository,Category.class);
        User user = getLoggedValidUserFromSession(session, request);
        if (category.getUserId() == UserDao.DEFAULT_CATEGORY_USER_ID || user.getUserId() == category.getUserId()) {
            return category;
        }
        throw new ForbiddenRequestException("Forbidden request.");
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
    private void validateCategoryName(long userId, String categoryName) throws InvalidRequestDataException {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new InvalidRequestDataException("No category name input.");
        }
        List<Category> categories = categoryDao.getPredefinedAndUserCategories(userId);
        for (Category category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                throw new InvalidRequestDataException("Category already exists.");
            }
        }
    }
    void validateCategoryAndUserOwnership(User user, Category category) throws NotFoundException {
        if (category == null || (category.getUserId() != null && user.getUserId() != category.getUserId())) {
            throw new NotFoundException("Category not found.");
        }
    }
    private void validateImage(long imageId) throws NotFoundException {
        if (imageDao.getImageById(imageId) == null) {
            throw new NotFoundException("Image not found.");
        }
    }
}
