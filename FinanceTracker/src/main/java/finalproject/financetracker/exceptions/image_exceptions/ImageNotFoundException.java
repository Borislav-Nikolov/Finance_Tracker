package finalproject.financetracker.exceptions.image_exceptions;

import finalproject.financetracker.exceptions.MyException;

public class ImageNotFoundException extends MyException {
    public ImageNotFoundException() {
        super("Image not found.");
    }
}
