package finalproject.financetracker.model.exceptions.category_exceptions;

public class CategoryNotFoundException extends CategoryException {
    public CategoryNotFoundException() {
        super("Category not found.");
    }
}
