package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.PlannedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlannedTransactionRepo extends JpaRepository<PlannedTransaction, Long > {

    List<PlannedTransaction> findAllByUserId(long userId);
    List<PlannedTransaction> findAllByAccountId(long accountId);
    int deleteByPtId(long transactionId);
}
