package ua.nure.finance.reposotory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Income;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends CrudRepository<Income, Long> {
    List<Income> findByOperationDateBetween(LocalDate startDate, LocalDate endDate);
}
