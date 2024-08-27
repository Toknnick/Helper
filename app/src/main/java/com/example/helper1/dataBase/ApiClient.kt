package com.example.helper1.dataBase

import retrofit2.Callback
import retrofit2.Retrofit

class ApiClient(private val retrofit: Retrofit) {
    private val apiInterface: ApiInterface = retrofit.create(ApiInterface::class.java)

    fun createUser(user: User, callback: Callback<User>) {
        val call = apiInterface.createUser(user)
        call.enqueue(callback)
    }

    fun getUserByLogin(login: String, callback: Callback<User>) {
        val call = apiInterface.getUserByLogin(login)
        call.enqueue(callback)
    }

    fun deleteUser(login: String, callback: Callback<Void>) {
        val call = apiInterface.deleteUser(login)
        call.enqueue(callback)
    }
}