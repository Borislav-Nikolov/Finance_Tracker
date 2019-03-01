package finalproject.financetracker.model.dtos;

import finalproject.financetracker.model.exceptions.InvalidRequestDataException;

public interface IRequestDTO {
    void checkValid() throws InvalidRequestDataException;
}
