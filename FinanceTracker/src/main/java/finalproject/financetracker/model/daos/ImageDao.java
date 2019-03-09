package finalproject.financetracker.model.daos;

import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ImageDao {
    @Autowired
    private ImageRepository imageRepository;
    public static String ICONS_ABSOLUTE_PATH = "C:/img/category_icons/";

    public  Image getImageById(long id) {
        return imageRepository.findByImageId(id);
    }
    public Image getImageByFileName(String fileName) {
        return imageRepository.findByUri(fileName);
    }
    public void addImage(String fileName) {
        Image image = new Image();
        image.setUri(fileName);
        imageRepository.save(image);
    }

    public void addAllIcons() throws NotFoundException {
        File dir = new File(ICONS_ABSOLUTE_PATH);
        if (!dir.exists()) {
            throw new NotFoundException("Images not found");
        }
        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }
    }
}
