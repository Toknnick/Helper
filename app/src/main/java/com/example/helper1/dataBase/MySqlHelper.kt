package com.example.helper1.dataBase

import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MySqlHelper {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    //User
    fun createUser(user: User, callback: Callback<User>) {
        apiService.createUser(user).enqueue(callback)
    }

    fun getUser(login: String, callback: Callback<User>) {
        apiService.getUser(login).enqueue(callback)
    }

    fun deleteUser(login: String, callback: Callback<Unit>) {
        apiService.deleteUser(login).enqueue(callback)
    }

    //Room


    //Task


    //Event
}
