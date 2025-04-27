package ua.nure.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.TransactionView;
import ua.nure.finance.model.TransactionViewId;

import java.time.LocalDate;
import java.util.List;

public interface TransactionViewRepository extends JpaRepository<TransactionView, TransactionViewId> {

    List<TransactionView> findByOperationDateBetweenOrderByOperationDateDesc(LocalDate startDate, LocalDate endDate);

}
