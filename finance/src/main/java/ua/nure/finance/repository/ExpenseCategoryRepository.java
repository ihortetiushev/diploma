package ua.nure.finance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.ExpenseCategory;

@Repository
public interface ExpenseCategoryRepository extends CrudRepository<ExpenseCategory, Long> {
}
