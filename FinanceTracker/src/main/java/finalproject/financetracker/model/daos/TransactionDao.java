package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionDao {

    @Autowired
    private TransactionRepo repo;
}
