package ua.nure.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.OptimizationCategory;

import java.util.List;

public interface OptimizationCategoryRepository extends JpaRepository<OptimizationCategory, Long> {
    List<OptimizationCategory> findAllByOrderBySortOrderAscIdAsc();
}
