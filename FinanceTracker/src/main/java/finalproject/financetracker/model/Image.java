package finalproject.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Image {
    private long imageId;
    private String uri;

    public Image(long imageId, String imageUri) {
        this.imageId = imageId;
        this.uri = imageUri;
    }
}
