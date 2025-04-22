package ua.nure.finance.reposotory;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.AssetsCategory;

public interface AssetsCategoryRepository extends JpaRepository<AssetsCategory, Long> {
}
