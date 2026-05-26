package edu.cit.salonga.assetflow.network

import retrofit2.Response
import retrofit2.http.GET

interface CategoryService {

    @GET("api/categories")
    suspend fun getAllCategories(): Response<List<String>>
}
