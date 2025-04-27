package ua.nure.finance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Expense;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends CrudRepository<Expense, Long> {
    List<Expense> findByOperationDateBetween(LocalDate startDate, LocalDate endDate);
}
