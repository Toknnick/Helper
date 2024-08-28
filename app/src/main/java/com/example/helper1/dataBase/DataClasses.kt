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

/*data class Task(
    //var idTask: Int,
    var data: String,
    override var time: String,
    var name: String,
    var points: List<String>,
    var checkBoxes: List<Boolean>
) : Timable*/

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

class DataClasses {
}