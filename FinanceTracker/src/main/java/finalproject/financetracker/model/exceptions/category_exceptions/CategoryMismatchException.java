package finalproject.financetracker.model.exceptions.category_exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus()
public class CategoryMismatchException extends CategoryException {
    public CategoryMismatchException() {
        super("This category doesn't belong to that user.");
    }
}
