package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {

    List<Transaction> findAllByUserId(long userId);
}
