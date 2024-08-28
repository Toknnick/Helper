package com.example.helper1.dataBase

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MySQLController(private val apiClient: ApiClient) {
    interface CreateMessageCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
    }


    interface IsExistUserCallback {
        fun onSuccess(isExist: Boolean)
        fun onFailure(isExist: Boolean)
    }

    interface CreateUserCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
        fun onUserCreated(user: User)
    }


    interface CreateRoomCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String)
        fun onRoomCreated(idRoom: Long)
    }

    interface GetAllRoomsCallback {
        fun onSuccess(rooms: List<Room>)
        fun onFailure(message: String)
    }

    interface GetRoomCallback {
        fun onSuccess(gotRoom: Room)
        fun onFailure(message: String)
    }

    interface GetAllEventsCallback {
        fun onSuccess(events: List<Event>)
        fun onFailure(message: String)
    }
    interface GetAllTaskCallback {
        fun onSuccess(events: List<Task>)
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

    fun getAllEvents(idRoom: Long, callback: GetAllEventsCallback) {
        apiClient.getAllEvents(idRoom, object : Callback<List<Event>> {
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
        apiClient.getEvent(previousEvent,object : Callback<Event>{
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                val foundedEvent = response.body()
                if(foundedEvent!=null) {
                    updatingEvent.idEvent = foundedEvent.idEvent
                }

                apiClient.updateEvent(updatingEvent,object : Callback<Void>{
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Успех!")
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

    fun deleteEvent(deletingEvent: Event, callback: CreateMessageCallback){
        apiClient.getEvent(deletingEvent,object : Callback<Event>{
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                val foundedEvent = response.body()
                if(foundedEvent!=null) {
                    deletingEvent.idEvent = foundedEvent.idEvent
                }

                apiClient.deleteEvent(deletingEvent, object : Callback<Void>{
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


    fun createTask(task: Task, callback: CreateMessageCallback) {
        apiClient.createTask(task, object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                val createdTask = response.body()

                Log.d("MyTag",task.toString())
                Log.d("MyTag",createdTask.toString())

                if (createdTask != null) {
                    callback.onSuccess("Успешно")
                } else {
                    callback.onFailure("Ошибка1 создания задачи")
                }
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                callback.onFailure("Ошибка2 создания задачи")
            }
        })
    }



    fun getAllTasks(idRoom: Long, callback: GetAllTaskCallback) {
        apiClient.getAllTasks(idRoom, object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    val tasks = response.body()
                    if (tasks != null) {
                        callback.onSuccess(tasks)
                    } else {
                        callback.onFailure("Ошибка1 получения задач")
                    }
                } else {
                    callback.onFailure("Ошибка2 получения задач")
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                callback.onFailure("Ошибка3 получения задач")
            }
        })
    }

    fun updateTask(previousTask: Task, updatingTask: Task, callback: CreateMessageCallback){
        apiClient.getTask(previousTask,object : Callback<Task>{
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                val foundedTask = response.body()
                if(foundedTask!=null) {
                    updatingTask.idTask = foundedTask.idTask
                }

                apiClient.updateTask(updatingTask,object : Callback<Void>{
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Успех!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось обновить!")
                    }
                })
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено событие")
            }
        })
    }

    fun deleteTask(deletingTask: Task, callback: CreateMessageCallback){
        apiClient.getTask(deletingTask,object : Callback<Task>{
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                val foundedTask = response.body()
                if(foundedTask!=null) {
                    deletingTask.idTask = foundedTask.idTask
                }

                apiClient.deleteTask(deletingTask, object : Callback<Void>{
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Удалил!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось обновить!")
                    }
                })
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено событие")
            }
        })
    }
}