package finalproject.financetracker.model.dtos.categoryDTOs;

import finalproject.financetracker.model.pojos.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryInfoDTO {
    private long categoryId;
    private String categoryName;
    private boolean isIncome;
    private long userId;
    private long imageId;
    private Image image;
}
