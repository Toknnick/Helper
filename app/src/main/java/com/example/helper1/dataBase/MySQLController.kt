package com.example.helper1.dataBase

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MySQLController(private val apiClient: ApiClient) {
    interface CreateCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
    }

    fun createUser(user: User, callback: CreateCallback) {
        apiClient.getUserByLogin(user.login, object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                callback.onFailure("Ошибка! Такой логин уже существует!")
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                apiClient.createUser(user, object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {}

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        callback.onSuccess("Пользователь создан успешно")
                    }
                })
            }
        })
    }

    fun updateUser(newUser: User, callback: CreateCallback,isGetNewRoom: Boolean){
        apiClient.getUserByLogin(newUser.login, object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if (user != null) {
                    newUser.ownRoom = user.ownRoom
                    if(!isGetNewRoom) {
                        newUser.availableRooms = user.availableRooms
                    }
                }
                apiClient.updateUser(newUser, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        apiClient.createUser(newUser, object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {}

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                callback.onSuccess("Пользователь обновлен успешно")
                            }
                        })
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }

            override fun onFailure(call: Call<User>, t: Throwable) {}
        })
    }
}