package edu.cit.salonga.assetflow.features.borrow.model

data class BorrowRequestDto(
    val id: String?,
    val userId: String?,
    val userName: String?,
    val userEmail: String?,
    val assetId: String?,
    val assetName: String?,
    val requestDate: String?,
    val dueDate: String?,
    val status: String?,
    val requestDateTime: String?,
    val statusUpdatedAt: String?,
    val rejectionNote: String?
)
