package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletetUserRepository extends JpaRepository<User, Long> {
}
