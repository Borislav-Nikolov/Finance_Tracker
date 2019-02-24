package finalproject.financetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Category {
    private long categoryId;
    private String categoryName;
    private boolean isIncome;
    private long userId;
    private Image image;

    public Category(String categoryName, boolean isIncome, long userId, Image image) {
        this.categoryName = categoryName;
        this.isIncome = isIncome;
        this.userId = userId;
        this.image = image;
    }
}
