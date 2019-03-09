package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.ImageDao;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class ImageController {
    @GetMapping(value="/images/{name}", produces = "image/png")
    public byte[] downloadImage(@PathVariable("name") String imageName) throws IOException {
        File newImage = new File(ImageDao.ICONS_ABSOLUTE_PATH +imageName);
        FileInputStream fis = new FileInputStream(newImage);
        return IOUtils.toByteArray(fis);
    }
}
