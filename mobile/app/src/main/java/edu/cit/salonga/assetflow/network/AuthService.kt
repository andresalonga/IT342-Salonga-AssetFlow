package edu.cit.salonga.assetflow.network

import edu.cit.salonga.assetflow.features.auth.model.AuthResponse
import edu.cit.salonga.assetflow.features.auth.model.LoginRequest
import edu.cit.salonga.assetflow.features.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google/mobile")
    suspend fun googleMobile(@Body request: edu.cit.salonga.assetflow.features.auth.model.GoogleAuthRequest): Response<AuthResponse>
}
