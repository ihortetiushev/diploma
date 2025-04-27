package ua.nure.finance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.nure.finance.model.Currency;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, String> {
}
