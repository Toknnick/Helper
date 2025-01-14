package com.example.helper1.dataBase

interface Timable {
    var time: String
    var date: String
}

data class Event(
    var idEvent: Int,
    var idRoom: Int,
    override var date: String,
    override var time: String,
    var place: String,
    var event: String
) : Timable

data class Task(
    var idTask: Int,
    var idRoom: Int,
    override var date: String,
    override var time: String,
    var name: String,
    var points: String,
    var checkBoxes: String
) : Timable

data class Room(
    var idRoom: Int,
    var name: String,
    var password: String,
    var single: Boolean,
    var owner: String,
    var users: String,
    var bannedUsers: String
)

data class User(
    var login: String,
    var password: String,
    var ownRoom: Int,
    var availableRooms: String
)

data class Image(
    var idImage: Int,
    var idRoom: Int,
    override var date: String,
    override var time: String,
    var url : String,
):Timable

data class File(
    var idFile: Int,
    var idRoom: Int,
    override var date: String,
    override var time: String,
    var url : String,
):Timable

interface CreateMessageCallback {
    fun onSuccess(message: String)
    fun onFailure(message: String)
}


interface IsExistUserCallback {
    fun onSuccess(isExist: Boolean)
    fun onFailure(isExist: Boolean)
}

interface GetUserCallback {
    fun onSuccess(user: User)
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
    fun onRoomCreated(idRoom: Int)
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

interface GetAllImagesCallback {
    fun onSuccess(images: List<Image>)
    fun onFailure(message: String)
}

interface GetAllFilesCallback {
    fun onSuccess(files: List<File>)
    fun onFailure(message: String)
}

class DataClasses {
}