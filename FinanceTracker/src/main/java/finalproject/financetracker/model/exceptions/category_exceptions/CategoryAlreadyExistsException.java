package finalproject.financetracker.model.exceptions.category_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class CategoryAlreadyExistsException extends CategoryException {
    public CategoryAlreadyExistsException() {
        super("A category by that name already exists.");
    }
}
