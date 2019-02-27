package finalproject.financetracker.model.exceptions.image_exceptions;

import finalproject.financetracker.model.exceptions.MyException;

public class ImageNotFoundException extends MyException {
    public ImageNotFoundException() {
        super("Image not found.");
    }
}
