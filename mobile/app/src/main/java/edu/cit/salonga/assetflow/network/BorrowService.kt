package edu.cit.salonga.assetflow.network

import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestCreateDto
import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestDto
import retrofit2.Response
import retrofit2.http.*

interface BorrowService {

    @POST("api/assets/{assetId}/borrow")
    suspend fun submitBorrowRequest(
        @Path("assetId") assetId: Long,
        @Body request: BorrowRequestCreateDto
    ): Response<BorrowRequestDto>

    @GET("api/assets/borrow-requests")
    suspend fun getAllBorrowRequests(): Response<List<BorrowRequestDto>>

    @PATCH("api/assets/borrow-requests/{requestId}")
    suspend fun updateBorrowRequestStatus(
        @Path("requestId") requestId: Long,
        @Body body: Map<String, String>
    ): Response<BorrowRequestDto>

    @GET("api/assets/my-requests")
    suspend fun getMyBorrowRequests(): Response<List<BorrowRequestDto>>
}
