package finalproject.financetracker.model.repositories;

import finalproject.financetracker.model.pojos.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryNameAndUserId(String categoryName, long userId);
    List<Category> findAllByUserId(long userId);
    Category findByCategoryId(long categoryId);
}
