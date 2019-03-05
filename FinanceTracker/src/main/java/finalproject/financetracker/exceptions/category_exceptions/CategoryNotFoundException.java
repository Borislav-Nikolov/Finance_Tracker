package finalproject.financetracker.exceptions.category_exceptions;

public class CategoryNotFoundException extends CategoryException {
    public CategoryNotFoundException() {
        super("Category not found.");
    }
}
