package finalproject.financetracker.controller;

import finalproject.financetracker.exceptions.InvalidRequestDataException;
import finalproject.financetracker.exceptions.MyException;
import finalproject.financetracker.exceptions.NotFoundException;
import finalproject.financetracker.model.daos.ImageDao;
import finalproject.financetracker.model.pojos.Image;
import finalproject.financetracker.model.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RestController
@RequestMapping(produces = "application/json")
public class ImageController extends AbstractController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping(value="/images/{name}", produces = "image/png")
    public byte[] downloadImage(@PathVariable(value = "name") String imageName)
            throws IOException, MyException {
        return getImageFromName(imageName);
    }
    @GetMapping(value="/images", produces = "image/png")
    public byte[] downloadImageById(@RequestParam(value = "imageId", required = false) String imageId)
            throws IOException, MyException {
        if (imageId == null) {
            throw new InvalidRequestDataException("No image id was input.");
        }
        Image image = imageRepository.findByImageId(parseLong(imageId));
        if (image == null) {
            throw new NotFoundException("Image not found.");
        }
        String imageName = image.getUri();
        return getImageFromName(imageName);
    }
    private byte[] getImageFromName(String imageName) throws NotFoundException, IOException {
        File newImage = new File(ImageDao.ICONS_ABSOLUTE_PATH +imageName);
        if (!newImage.exists()) {
            throw new NotFoundException("Image not found.");
        }
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
