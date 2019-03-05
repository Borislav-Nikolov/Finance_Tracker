package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ImageDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ImageRepository imageRepository;

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

    public void addAllIcons() {
        File dir = new File(ICONS_RELATIVE_PATH);
        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }
    }
}
