package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User getByUserId(long userId);
}
