package com.example.helper1.dataBase

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MySQLController(private val apiClient: ApiClient) {
    interface CreateUserCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
    }

    fun createUser(user: User, callback: CreateUserCallback) {
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
}