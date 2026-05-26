package edu.cit.salonga.assetflow.features.assets.model

data class AssetDto(
    val id: String?,
    val name: String?,
    val serialNumber: String?,
    val category: String?,
    val status: String?,
    val description: String?,
    val imageUrl: String?,
    val createdAt: String?
)
