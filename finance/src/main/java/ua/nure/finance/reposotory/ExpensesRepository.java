package ua.nure.finance.reposotory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Expenses;

@Repository
public interface ExpensesRepository extends CrudRepository<Expenses, Long>{
}
