package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {
    List<Transaction> findAllByAccountId(long accountId);
    int deleteByAccountId(long accountId);
    int deleteByTransactionId(long transactionId);
}
