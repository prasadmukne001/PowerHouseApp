package com.example.powerhouseapp.network

import com.example.powerhouseapp.model.ImageUploadResponse
import com.example.powerhouseapp.model.UserConfigRequest
import com.example.powerhouseapp.model.UserConfigResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface ApiInterface {

    companion object {

        var BASE_URL = "https://countgo-n34mdgp5qq-as.a.run.app"

        fun create(): ApiInterface {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build()
            val retrofit = Retrofit.Builder()
                //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiInterface::class.java)

        }
    }

    @Headers("Content-Type: application/json")
    @POST("/config")
    fun getUserConfig(@Body userConfigRequest: UserConfigRequest): Call<UserConfigResponse>

    @Multipart
    @POST("/upload_image")
    fun uploadImage(
        @Query("email_id") emailId: String,
        @Part image: MultipartBody.Part
    ): Call<ImageUploadResponse>

    @Multipart
    @POST("/upload_depth")
    fun uploadDepth(
        @Query("email_id") emailId: String,
        @Part image: MultipartBody.Part
    ): Call<ImageUploadResponse>
}