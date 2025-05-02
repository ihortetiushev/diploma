package ua.nure.finance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.ExpenseCategory;

import java.util.List;

@Repository
public interface ExpenseCategoryRepository extends CrudRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findAll();
}
