package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.Room
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomManager(private val apiClient: ApiClient) {
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

    fun updateRoom(newRoom: Room, callback: CreateMessageCallback){
        apiClient.getRoom(newRoom.idRoom, object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val foundedRoom = response.body()
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

    fun getRoom(idRoom: Long, callback: GetRoomCallback){
        apiClient.getRoom(idRoom, object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val gotRoom = response.body()
                if(gotRoom != null) {
                    callback.onSuccess(gotRoom)
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                callback.onFailure("Неверный номер комнаты")
            }
        })
    }
}