package finalproject.financetracker.model.dtos.categoryDTOs;

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
    private Long userId;
    private long imageId;
    private String imageUri;
}
