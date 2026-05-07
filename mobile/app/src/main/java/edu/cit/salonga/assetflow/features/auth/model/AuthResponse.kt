package edu.cit.salonga.assetflow.features.auth.model

data class AuthResponse(
    val message: String,
    val userId: Long?,
    val name: String?,
    val email: String?,
    val role: String?,
    val token: String?,
    val success: Boolean
)
