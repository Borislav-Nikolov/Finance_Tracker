package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
}
