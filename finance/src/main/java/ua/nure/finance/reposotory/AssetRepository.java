package ua.nure.finance.reposotory;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.Asset;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Add custom query methods if needed
}