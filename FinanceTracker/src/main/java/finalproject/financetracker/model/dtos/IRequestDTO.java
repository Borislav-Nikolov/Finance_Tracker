package finalproject.financetracker.model.dtos;

import finalproject.financetracker.exceptions.InvalidRequestDataException;

public interface IRequestDTO {
    void checkValid() throws InvalidRequestDataException;
}
