interface ApiService {
    @Multipart
    @POST("/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): Response<Void>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://yourserver.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
