package ua.nure.finance.reposotory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Income;

@Repository
public interface IncomeRepository extends CrudRepository<Income, Long> {
}
