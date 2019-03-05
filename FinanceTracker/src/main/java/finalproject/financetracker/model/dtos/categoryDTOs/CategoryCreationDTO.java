package finalproject.financetracker.model.dtos.categoryDTOs;

import finalproject.financetracker.model.dtos.IRequestDTO;
import finalproject.financetracker.exceptions.InvalidRequestDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryCreationDTO implements IRequestDTO {
    private String categoryName;
    private boolean isIncome;
    private long imageId;

    @Override
    public void checkValid() throws InvalidRequestDataException {
        if (categoryName == null) throw new InvalidRequestDataException("Category name is null.");
    }
}
