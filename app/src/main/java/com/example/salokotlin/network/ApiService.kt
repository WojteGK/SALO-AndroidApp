package com.example.salokotlin.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("/")
    suspend fun uploadImageWithHeaders(
        @Part file: MultipartBody.Part,
        @HeaderMap headers: Map<String, String> // Add headers to the request
    ): Response<ResponseBody>

    @POST("/assignments")
    suspend fun sendGroupAssignments(
        @HeaderMap headers: Map<String, String>,
        @Body assignments: Map<String, String> // Sending as JSON
    ): Response<Unit> // Response<Unit> since we don't need to process the response
}