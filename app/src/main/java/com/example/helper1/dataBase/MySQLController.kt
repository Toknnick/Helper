package com.example.helper1.dataBase

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MySQLController(private val apiClient: ApiClient) {
    interface CreateMessageCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
    }

    interface CreateIsExistUserCallback {
        fun onSuccess(isExist: Boolean)
        fun onFailure(isExist: Boolean)
    }

    interface CreateRoomCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
        fun onRoomCreated(idRoom: Long)
    }

    interface CreateUserCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
        fun onUserCreated(user: User)
    }




    interface GetAllRoomsCallback {
        fun onSuccess(rooms: List<Room>)
        fun onFailure(message: String)
    }

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
                        apiClient.updateUser(newUser, object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                callback.onSuccess("Пользователь обновлен успешно")
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                callback.onFailure("Ошибка обновления пользователя")
                            }
                        })
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка обновления пользователя")
                    }
                })
            }

            override fun onFailure(call: Call<User>, t: Throwable) {}
        })
    }

    fun getUser(login:String, callback: CreateIsExistUserCallback){
        apiClient.getUserByLogin(login, object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                callback.onSuccess(true)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                callback.onFailure(false)
            }
        })
    }

    fun getAllRooms(callback: GetAllRoomsCallback) {
        apiClient.getAllRooms(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful) {
                    val rooms = response.body()
                    if (rooms != null) {
                        callback.onSuccess(rooms)
                    } else {
                        callback.onFailure("Ошибка получения комнат")
                    }
                } else {
                    callback.onFailure("Ошибка получения комнат")
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                callback.onFailure("Ошибка получения комнат")
            }
        })
    }

    //TODO: передалать методы, связанные с юзером
    fun createRoom(room: Room, callback: CreateRoomCallback) {
        apiClient.createRoom(room, object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val createdRoom = response.body()
                if (createdRoom != null) {
                    callback.onSuccess("Комната создана успешно")
                    callback.onRoomCreated(createdRoom.idRoom)
                } else {
                    callback.onFailure("Ошибка создания комнаты")
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                callback.onFailure("Ошибка создания комнаты")
            }
        })
    }

    fun updateRoom(newRoom: Room, callback: CreateMessageCallback){
        apiClient.getRoom(newRoom.idRoom, object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val foundedRoom = response.body()
                newRoom.name = foundedRoom?.name.toString()
                apiClient.updateRoom(newRoom, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Комната обновлена успешно")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {}
        })
    }
}