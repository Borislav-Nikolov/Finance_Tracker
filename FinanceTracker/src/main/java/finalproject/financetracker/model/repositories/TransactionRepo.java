package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {

    List<Transaction> findAllByUserId(long userId);
    List<Transaction> findAllByAccountId(long accountId);
    List<Transaction> findAllByAccountIdAndUserId(long accountId, long userId);
    int deleteByAccountId(long accountId);
    int deleteByAccountIdAndUserId(long accountId, long userId);
    int deleteByTransactionId(long transactionId);
}
