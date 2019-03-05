package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionDao {

    @Autowired
    private TransactionRepo repo;
}
