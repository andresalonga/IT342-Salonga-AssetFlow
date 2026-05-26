package edu.cit.salonga.assetflow.features.assets.repository;

import edu.cit.salonga.assetflow.features.assets.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySerialNumber(String serialNumber);
    List<Asset> findByStatus(Asset.AssetStatus status);
    List<Asset> findByCategoryId(Long categoryId);
}
