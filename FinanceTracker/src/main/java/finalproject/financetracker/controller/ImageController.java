package finalproject.financetracker.controller;

import finalproject.financetracker.model.daos.ImageDao;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@RequestMapping(produces = "application/json")
public class ImageController {
    @GetMapping(value="/images/{name}", produces = "image/png")
    public byte[] downloadImage(@PathVariable("name") String imageName) throws IOException {
        File newImage = new File(ImageDao.ICONS_ABSOLUTE_PATH +imageName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(newImage);
        int nRead;
        byte[] data = new byte[1048576];  // max picture size

        while ((nRead = fis.read(data, 0, data.length)) != -1) {
            byteArrayOutputStream.write(data, 0, nRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
