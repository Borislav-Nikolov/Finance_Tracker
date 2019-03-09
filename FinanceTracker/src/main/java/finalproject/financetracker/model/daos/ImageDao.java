package finalproject.financetracker.model.daos;

import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.repositories.ImageRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class ImageDao {
    @Autowired
    private ImageRepository imageRepository;
    private static org.apache.log4j.Logger logger = LogManager.getLogger(Logger.class);
    public static String ICONS_ABSOLUTE_PATH = "C:/img/category_icons/";

    public Image getImageById(long id) {
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

    public void addAllIcons(){
        File dir = new File(ICONS_ABSOLUTE_PATH);
        if (!dir.exists()) {
            logger.info("Image directory does not exist! ...creating... >" + ICONS_ABSOLUTE_PATH);
            try {
                if (dir.mkdirs()) {
                    logger.info("Image directory created successfully!");
                }
            } catch (Exception e) {
                logger.error("Error creating image directory " + ICONS_ABSOLUTE_PATH, e);
            }
        }

        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }

    }
}
