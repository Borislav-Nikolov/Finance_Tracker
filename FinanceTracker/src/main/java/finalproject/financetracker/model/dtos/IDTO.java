package finalproject.financetracker.model.dtos;

import finalproject.financetracker.model.exceptions.InvalidRequestDataException;

public interface IDTO {

    void checkValid() throws InvalidRequestDataException;
}
