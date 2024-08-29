package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateUserCallback
import com.example.helper1.dataBase.IsExistUserCallback
import com.example.helper1.dataBase.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserManager(private val apiClient: ApiClient) {
    fun createUser(user: User, callback: CreateUserCallback) {
        apiClient.createUser(user, object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val createdUser = response.body()
                if (createdUser != null) {
                    callback.onSuccess("Пользователь создан успешно")
                    callback.onUserCreated(createdUser)
                } else {
                    callback.onFailure("Ошибка создания пользователя")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {}
        })
    }

    fun updateUser(newUser: User, callback: CreateMessageCallback, isGetNewRoom: Boolean){
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
                        callback.onSuccess("Пользователь обновлен успешно")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка обновления пользователя")
                    }
                })
            }

            override fun onFailure(call: Call<User>, t: Throwable) {}
        })
    }

    fun getUser(login:String, callback: IsExistUserCallback){
        apiClient.getUserByLogin(login, object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                callback.onSuccess(true)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                callback.onFailure(false)
            }
        })
    }
}