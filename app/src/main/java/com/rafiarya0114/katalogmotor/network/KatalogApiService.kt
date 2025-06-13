package com.rafiarya0114.katalogmotor.network

import com.rafiarya0114.katalogmotor.model.Katalog
import com.rafiarya0114.katalogmotor.model.OpStatus
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

private const val BASE_URL = "https://fd2c-35-201-161-34.ngrok-free.app/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface KatalogApiService {
    @GET("katalog")
    suspend fun getKatalog(
        @Header("Authorization") token: String
    ): List<Katalog>

    @Multipart
    @POST("katalog")
    suspend fun postKatalog(
        @Header("Authorization") token: String,
        @Part("judul") nama: RequestBody,
        @Part("manufacturer") namaLatin: RequestBody,
        @Part("harga") harga: Double,
        @Part image: MultipartBody.Part
    ): OpStatus

    @DELETE("katalog/{id_katalog}")
    suspend fun deleteKatalog(
        @Header("Authorization") token: String,
        @Path("id_katalog") idKatalog: Long
    ): OpStatus

    @Multipart
    @POST("katalog/{id_katalog}")
    suspend fun updateKatalog(
        @Header("Authorization") token: String,
        @Part("_method") method: RequestBody,
        @Path("id_katalog") idKatalog: Long,
        @Part("judul") nama: RequestBody,
        @Part("manufacturer") namaLatin: RequestBody,
        @Part("harga") harga: Double,
        @Part image: MultipartBody.Part? = null,
    ): OpStatus

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") nama: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): OpStatus
}

object KatalogApi {
    val service: KatalogApiService by lazy {
        retrofit.create(KatalogApiService::class.java)
    }

    fun getImageUrl(id: Long): String {
        return "${BASE_URL}katalog/image/$id?timestamp=${System.currentTimeMillis()}"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }