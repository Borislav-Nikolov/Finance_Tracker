package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    VerificationToken findByUserId(long userId);
}
