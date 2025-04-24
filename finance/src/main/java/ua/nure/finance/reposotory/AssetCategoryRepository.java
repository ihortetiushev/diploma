package ua.nure.finance.reposotory;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.AssetCategory;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
}
