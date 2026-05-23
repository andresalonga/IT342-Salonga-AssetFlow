package edu.cit.salonga.assetflow.features.assets.dto;

public class AssetCreateDto {
    public String name;
    public String serialNumber;
    public String category;
    public String status;
    public String description;
    public String imageUrl;

    public AssetCreateDto() {}

    public AssetCreateDto(
            String name,
            String serialNumber,
            String category,
            String status,
            String description,
            String imageUrl
    ) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.category = category;
        this.status = status;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
