package ua.nure.finance.reposotory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Expenses;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpensesRepository extends CrudRepository<Expenses, Long> {
    List<Expenses> findByOperationDateBetween(LocalDate startDate, LocalDate endDate);
}
