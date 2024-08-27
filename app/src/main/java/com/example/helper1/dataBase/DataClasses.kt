package com.example.helper1.dataBase

interface Timable {
    var time: String
}

data class Event(
    var data: String,
    override var time: String,
    var place: String,
    var event: String
) : Timable

data class Task(
    var data: String,
    override var time: String,
    var name: String,
    var points: List<String>,
    var checkBoxes: List<Boolean>
) : Timable

data class Room(
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