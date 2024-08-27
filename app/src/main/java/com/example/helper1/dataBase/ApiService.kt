package com.example.helper1.dataBase
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("users/create")
    fun createUser(@Body user: User): Call<User>

    @GET("users/get/{login}")
    fun getUser(@Path("login") login: String): Call<User>

    @DELETE("users/delete/{login}")
    fun deleteUser(@Path("login") login: String): Call<Unit>
}