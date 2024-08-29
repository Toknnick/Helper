package com.example.helper1.dataBase

interface Timable {
    var time: String
}

data class Event(
    var idEvent: Long,
    var idRoom: Long,
    var date: String,
    override var time: String,
    var place: String,
    var event: String
) : Timable

data class Task(
    var idTask: Long,
    var idRoom: Long,
    var date: String,
    override var time: String,
    var name: String,
    var points: String,
    var checkBoxes: String
) : Timable

data class Room(
    var idRoom: Long,
    var name: String,
    var password: String
)

data class User(
    var login: String,
    var password: String,
    var ownRoom: Long,
    var availableRooms: String
)

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
    fun onSuccess(tasks: List<Task>)
    fun onFailure(message: String)
}

class DataClasses {
}