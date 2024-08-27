package com.example.helper1.dataBase

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterface {
    @POST("users/create")
    fun createUser(@Body user: User): Call<User>

    @GET("users/get/{login}")
    fun getUserByLogin(@Path("login") login: String): Call<User>

    @PUT("users/update")
    fun updateUser(@Body user: User): Call<Void>


    @POST("rooms/create")
    fun createRoom(@Body room: Room): Call<Room>

    @GET("rooms/get/{idRoom}")
    fun getRoom(@Path("idRoom") idRoom: Long): Call<Room>

    @PUT("rooms/update")
    fun updateRoom(@Body room: Room): Call<Room>

}