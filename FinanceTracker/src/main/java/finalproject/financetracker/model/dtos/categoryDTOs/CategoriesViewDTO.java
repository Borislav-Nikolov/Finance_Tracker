package finalproject.financetracker.model.dtos.categoryDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoriesViewDTO {
    private List<CategoryInfoDTO> categories;
}
