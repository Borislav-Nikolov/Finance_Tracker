package finalproject.financetracker.model.daos;

import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ImageDao {
    @Autowired
    private ImageRepository imageRepository;
    // TODO maybe use cloud storage
    private static String ICONS_RELATIVE_PATH = "src/main/resources/static/img/category_icons";

    public  Image getImageById(long id) {
        return imageRepository.findByImageId(id);
    }
    public Image getImageByFileName(String fileName) {
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        return imageRepository.findByUri(uri);
    }
    public void addImage(String fileName) {
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        Image image = new Image();
        image.setUri(uri);
        imageRepository.save(image);
    }

    public void addAllIcons() throws NotFoundException {
        File dir = new File(ICONS_RELATIVE_PATH);
        if (!dir.exists()) {
            throw new NotFoundException("Images not found");
        }
        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }
    }
}
