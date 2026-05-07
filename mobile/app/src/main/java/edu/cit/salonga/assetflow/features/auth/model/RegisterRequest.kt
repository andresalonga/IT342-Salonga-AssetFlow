package edu.cit.salonga.assetflow.features.auth.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
