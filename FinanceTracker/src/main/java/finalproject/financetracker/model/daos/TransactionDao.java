package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.Account;
import finalproject.financetracker.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionDao {

    @Autowired
    private TransactionRepo repo;

    public List<Transaction> getAllTByUserId(long userId) {
        return repo.findAllByUserId(userId);
    }

    public Transaction add(Transaction t) {
        return repo.save(t);
    }

    public void deleteT(long transactionId) {
        repo.deleteById(transactionId);
    }
}
