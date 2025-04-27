package ua.nure.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.nure.finance.model.TransactionView;

import java.time.LocalDate;
import java.util.List;

public interface TransactionViewRepository extends JpaRepository<TransactionView, Long> {

    @Query("""
        SELECT t FROM TransactionView t
        WHERE (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
          AND (:type IS NULL OR t.type = :type)
          AND (:startDate IS NULL OR t.operationDate >= :startDate)
          AND (:endDate IS NULL OR t.operationDate <= :endDate)
        ORDER BY t.operationDate DESC
    """)
    List<TransactionView> searchTransactions(
            @Param("description") String description,
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
