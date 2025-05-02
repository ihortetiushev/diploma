package ua.nure.finance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.IncomeCategory;

import java.util.Optional;

@Repository
public interface IncomeCategoryRepository extends CrudRepository<IncomeCategory, Long> {
    Optional<IncomeCategory> findByName(String name);
}
