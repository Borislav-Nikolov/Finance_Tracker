package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account,Long> {

}
