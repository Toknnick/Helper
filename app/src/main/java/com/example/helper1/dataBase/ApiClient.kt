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

    fun updateUser(user: User, callback: Callback<Void>) {
        val call = apiInterface.updateUser(user)
        call.enqueue(callback)
    }


    fun createRoom(room: Room, callback: Callback<Room>) {
        val call = apiInterface.createRoom(room)
        call.enqueue(callback)
    }

    fun getRoom(idRoom: Long, callback: Callback<Room>) {
        val call = apiInterface.getRoom(idRoom)
        call.enqueue(callback)
    }

    fun updateRoom(room: Room, callback: Callback<Room>) {
        val call = apiInterface.updateRoom(room)
        call.enqueue(callback)
    }
}