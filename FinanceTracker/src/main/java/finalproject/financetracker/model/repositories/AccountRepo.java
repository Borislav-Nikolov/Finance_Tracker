package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepo extends JpaRepository<Account,Long> {
    List<Account> findAllByUserId(long uderId);
}
