package com.example.salokotlin.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("/") // Root endpoint for IP:PORT (replace with exact path if needed)
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<Void> // Change the response type as per your server's response
}