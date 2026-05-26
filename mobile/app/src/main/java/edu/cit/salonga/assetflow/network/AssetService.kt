package edu.cit.salonga.assetflow.network

import edu.cit.salonga.assetflow.features.assets.model.AssetCreateDto
import edu.cit.salonga.assetflow.features.assets.model.AssetDto
import edu.cit.salonga.assetflow.features.assets.model.AssetUpdateDto
import retrofit2.Response
import retrofit2.http.*

interface AssetService {

    @GET("api/assets")
    suspend fun getAllAssets(): Response<List<AssetDto>>

    @GET("api/assets/{assetId}")
    suspend fun getAssetById(@Path("assetId") assetId: Long): Response<AssetDto>

    @POST("api/assets")
    suspend fun createAsset(@Body request: AssetCreateDto): Response<AssetDto>

    @PUT("api/assets/{assetId}")
    suspend fun updateAsset(
        @Path("assetId") assetId: Long,
        @Body request: AssetUpdateDto
    ): Response<AssetDto>

    @DELETE("api/assets/{assetId}")
    suspend fun deleteAsset(@Path("assetId") assetId: Long): Response<Unit>
}
