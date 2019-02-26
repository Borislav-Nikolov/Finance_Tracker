package finalproject.financetracker.model.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long categoryId;
    private String categoryName;
    private boolean isIncome;
    private long userId;
    private long imageId;
    @Transient
    private Image image;

    public Category(String categoryName, boolean isIncome, long userId, long imageId, Image image) {
        this.categoryName = categoryName;
        this.isIncome = isIncome;
        this.userId = userId;
        this.imageId = imageId;
        this.image = image;
    }
}
