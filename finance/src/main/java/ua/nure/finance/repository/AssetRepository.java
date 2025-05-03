package ua.nure.finance.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.finance.model.Asset;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByStatus(Asset.Status status, Sort sort);
    List<Asset> findByStatusAndCategory_Name(Asset.Status status, String categoryName);

}