package edu.cit.salonga.assetflow.models

data class AuthResponse(
    val message: String,
    val userId: Long?,
    val name: String?,
    val email: String?,
    val role: String?,
    val token: String?,
    val success: Boolean
)
