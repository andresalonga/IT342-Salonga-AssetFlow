package edu.cit.salonga.assetflow.features.assets.service;

import edu.cit.salonga.assetflow.features.assets.dto.AssetCreateDto;
import edu.cit.salonga.assetflow.features.assets.dto.AssetDto;
import edu.cit.salonga.assetflow.features.assets.dto.AssetUpdateDto;
import edu.cit.salonga.assetflow.features.assets.entity.Asset;
import edu.cit.salonga.assetflow.features.assets.entity.Category;
import edu.cit.salonga.assetflow.features.assets.repository.AssetRepository;
import edu.cit.salonga.assetflow.features.assets.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<AssetDto> getAll() {
        return assetRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AssetDto getById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        return toDto(asset);
    }

    public AssetDto create(AssetCreateDto request) {
        if (request == null) {
            throw new RuntimeException("Invalid request");
        }

        String name = normalizeRequired(request.name, "name");
        String serialNumber = normalizeRequired(request.serialNumber, "serialNumber");
        String categoryName = normalizeRequired(request.category, "category");

        assetRepository.findBySerialNumber(serialNumber).ifPresent(existing -> {
            throw new RuntimeException("Serial number already exists");
        });

        Asset asset = new Asset();
        asset.setName(name);
        asset.setSerialNumber(serialNumber);
        asset.setCategory(getOrCreateCategory(categoryName));
        asset.setStatus(parseStatus(request.status));
        asset.setDescription(normalizeOptional(request.description));
        asset.setImageUrl(normalizeOptional(request.imageUrl));

        return toDto(assetRepository.save(asset));
    }

    public AssetDto update(Long id, AssetUpdateDto request) {
        if (request == null) {
            throw new RuntimeException("Invalid request");
        }

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (request.name != null) {
            asset.setName(normalizeRequired(request.name, "name"));
        }

        if (request.serialNumber != null) {
            String serialNumber = normalizeRequired(request.serialNumber, "serialNumber");
            assetRepository.findBySerialNumber(serialNumber).ifPresent(existing -> {
                if (!Objects.equals(existing.getId(), asset.getId())) {
                    throw new RuntimeException("Serial number already exists");
                }
            });
            asset.setSerialNumber(serialNumber);
        }

        if (request.category != null) {
            String categoryName = normalizeRequired(request.category, "category");
            asset.setCategory(getOrCreateCategory(categoryName));
        }

        if (request.status != null) {
            asset.setStatus(parseStatus(request.status));
        }

        if (request.description != null) {
            asset.setDescription(normalizeOptional(request.description));
        }

        if (request.imageUrl != null) {
            asset.setImageUrl(normalizeOptional(request.imageUrl));
        }

        return toDto(assetRepository.save(asset));
    }

    public void delete(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new RuntimeException("Asset not found");
        }
        assetRepository.deleteById(id);
    }

    private AssetDto toDto(Asset asset) {
        String categoryName = asset.getCategory() != null ? asset.getCategory().getName() : null;
        String status = asset.getStatus() != null ? asset.getStatus().name().toLowerCase() : null;
        return new AssetDto(
                asset.getId(),
                asset.getName(),
                asset.getSerialNumber(),
                categoryName,
                status,
                asset.getDescription(),
                asset.getImageUrl(),
                asset.getCreatedAt()
        );
    }

    private Category getOrCreateCategory(String name) {
        Category existing = categoryRepository.findByName(name);
        if (existing != null) {
            return existing;
        }

        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private Asset.AssetStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return Asset.AssetStatus.AVAILABLE;
        }
        return Asset.AssetStatus.valueOf(status.trim().toUpperCase());
    }

    private String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required field: " + field);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
