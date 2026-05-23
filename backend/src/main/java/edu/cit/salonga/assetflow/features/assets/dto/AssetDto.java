package edu.cit.salonga.assetflow.features.assets.dto;

import java.time.LocalDateTime;

public class AssetDto {
    public String id;
    public String name;
    public String serialNumber;
    public String category;
    public String status;
    public String description;
    public String imageUrl;
    public String createdAt;

    public AssetDto() {}

    public AssetDto(
            Long id,
            String name,
            String serialNumber,
            String category,
            String status,
            String description,
            String imageUrl,
            LocalDateTime createdAt
    ) {
        this.id = id != null ? String.valueOf(id) : null;
        this.name = name;
        this.serialNumber = serialNumber;
        this.category = category;
        this.status = status;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }
}
