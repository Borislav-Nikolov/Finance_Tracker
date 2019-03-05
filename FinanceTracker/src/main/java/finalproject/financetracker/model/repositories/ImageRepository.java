package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByImageId(long imageId);
    Image findByUri(String uri);
}
