package ua.nure.finance.reposotory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.ExpensesCategory;
import ua.nure.finance.model.IncomeCategory;

@Repository
public interface ExpensesCategoryRepository extends CrudRepository<ExpensesCategory, Long> {
}
