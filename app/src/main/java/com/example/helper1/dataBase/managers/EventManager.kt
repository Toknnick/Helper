package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.Event
import com.example.helper1.dataBase.GetAllEventsCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventManager(private val apiClient: ApiClient) {
    fun createEvent(event: Event, callback: CreateMessageCallback) {
        apiClient.createEvent(event, object : Callback<Event> {
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                val createdEvent = response.body()
                if (createdEvent != null) {
                    callback.onSuccess("Успешно")
                } else {
                    callback.onFailure("Ошибка1 создания события")
                }
            }

            override fun onFailure(call: Call<Event>, t: Throwable) {
                callback.onFailure("Ошибка2 создания события")
            }
        })
    }

    fun getAllEvents(callback: GetAllEventsCallback) {
        apiClient.getAllEvents(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    if (events != null) {
                        callback.onSuccess(events)
                    } else {
                        callback.onFailure("Ошибка получения событий")
                    }
                } else {
                    callback.onFailure("Ошибка получения событий")
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                callback.onFailure("Ошибка получения событий")
            }
        })
    }

    fun getAllEventsByIdRoom(idRoom: Long, callback: GetAllEventsCallback) {
        apiClient.getAllEventsByIdRoom(idRoom, object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    if (events != null) {
                        callback.onSuccess(events)
                    } else {
                        callback.onFailure("Ошибка получения событий")
                    }
                } else {
                    callback.onFailure("Ошибка получения событий")
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                callback.onFailure("Ошибка получения событий")
            }
        })
    }

    fun updateEvent(previousEvent: Event, updatingEvent: Event, callback: CreateMessageCallback){
        updatingEvent.idEvent = previousEvent.idEvent
        apiClient.updateEvent(updatingEvent,object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                callback.onSuccess("Успех!")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback.onFailure("Ошибка! Не удалось обновить!")
            }
        })
    }

    fun deleteEvent(deletingEvent: Event, callback: CreateMessageCallback){
        apiClient.getEvent(deletingEvent,object : Callback<Event> {
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                val foundedEvent = response.body()
                if(foundedEvent!=null) {
                    deletingEvent.idEvent = foundedEvent.idEvent
                }

                apiClient.deleteEvent(deletingEvent, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Удалил!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось обновить!")
                    }
                })
            }

            override fun onFailure(call: Call<Event>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено событие")
            }
        })
    }
}