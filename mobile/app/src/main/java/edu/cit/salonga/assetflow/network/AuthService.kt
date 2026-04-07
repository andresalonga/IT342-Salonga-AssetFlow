package edu.cit.salonga.assetflow.network

import edu.cit.salonga.assetflow.models.AuthResponse
import edu.cit.salonga.assetflow.models.LoginRequest
import edu.cit.salonga.assetflow.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
