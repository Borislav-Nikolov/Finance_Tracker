package finalproject.financetracker.model.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long imageId;
    private String uri;

    public Image(long imageId, String imageUri) {
        this.imageId = imageId;
        this.uri = imageUri;
    }

    @Override
    public String toString() {
        return "Image{" +
                "imageId=" + imageId +
                ", uri='" + uri + '\'' +
                '}';
    }
}
