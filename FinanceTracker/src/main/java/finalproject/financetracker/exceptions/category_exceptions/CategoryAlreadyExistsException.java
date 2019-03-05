package finalproject.financetracker.exceptions.category_exceptions;

public class CategoryAlreadyExistsException extends CategoryException {
    public CategoryAlreadyExistsException() {
        super("A category by that name already exists.");
    }
}
