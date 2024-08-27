package com.example.helper1.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mydatabase", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY, data TEXT, time TEXT, place TEXT, event TEXT)")
        db.execSQL("CREATE TABLE tasks (_id INTEGER PRIMARY KEY, data TEXT, time TEXT, name TEXT, points TEXT, checkboxes TEXT)")
        db.execSQL("CREATE TABLE chosenDate (_id INTEGER PRIMARY KEY, chosen_date TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE roomId (_id INTEGER PRIMARY KEY, room_id TEXT DEFAULT '')")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS chosenDate")
        db.execSQL("DROP TABLE IF EXISTS roomId")
        onCreate(db)
    }

    fun insertEvent(event: Event) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("data", event.data)
        contentValues.put("time", event.time)
        contentValues.put("place", event.place)
        contentValues.put("event", event.event)
        db.insert("events", null, contentValues)
        db.close()
    }

    fun insertTask(task: Task) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("data", task.data)
        contentValues.put("time", task.time)
        contentValues.put("name", task.name)
        contentValues.put("points", task.points.joinToString(","))
        contentValues.put("checkboxes", task.checkBoxes.joinToString(","))
        db.insert("tasks", null, contentValues)
        db.close()
    }

    fun getEvents(): List<Event> {
        val db = readableDatabase
        val cursor = db.query("events", null, null, null, null, null, null)
        val events = ArrayList<Event>()
        while (cursor.moveToNext()) {
            val event = Event(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
            )
            events.add(event)
        }
        cursor.close()
        db.close()
        return events
    }

    fun getTasks(): List<Task> {
        val db = readableDatabase
        val cursor = db.query("tasks", null, null, null, null, null, null)
        val tasks = ArrayList<Task>()
        while (cursor.moveToNext()) {
            val task = Task(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4).split(",").toList(),
                cursor.getString(5).split(",").map { it.toBoolean() }.toList()
            )
            tasks.add(task)
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun getEventsByDate(date: String): List<Event> {
        val db = readableDatabase
        val cursor = db.query("events", null, "data = ?", arrayOf(date), null, null, null)
        val events = ArrayList<Event>()
        while (cursor.moveToNext()) {
            val event = Event(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
            )
            events.add(event)
        }
        cursor.close()
        db.close()
        return events
    }

    fun getTasksByDate(date: String): List<Task> {
        val db = readableDatabase
        val cursor = db.query("tasks", null, "data = ?", arrayOf(date), null, null, null)
        val tasks = ArrayList<Task>()
        while (cursor.moveToNext()) {
            val task = Task(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4).split(",").toList(),
                cursor.getString(5).split(",").map { it.toBoolean() }.toList()
            )
            tasks.add(task)
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun addRoomId(roomId :Int){
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("room_id", roomId.toString())
        db.insert("roomId", null, contentValues)
        db.close()
    }

    fun updateRoomId(roomId :Int) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("room_id", roomId.toString())
        db.update("roomId", contentValues, "_id =?", arrayOf("1"))
        db.close()
    }

    fun getRoomId():Int{
        val db = readableDatabase
        val cursor = db.query("roomId", null, null, null, null, null, null)
        var roomId = -1
        if (cursor.moveToFirst()) {
            roomId = cursor.getString(1).toInt()
        }
        cursor.close()
        db.close()
        return roomId
    }

    fun getChosenDate(): String {
        var db = readableDatabase
        val cursor = db.query("chosenDate", null, null, null, null, null, null)
        var chosenDate = ""

        if (cursor.moveToFirst()) {
            chosenDate = cursor.getString(1)
        } else {
            db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put("chosen_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            db.insert("chosenDate", null, contentValues)
            chosenDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        cursor.close()
        db.close()
        return chosenDate
    }

    fun deleteEvent(event: Event) {
        val db = writableDatabase
        db.delete("events", "data = ? AND time = ? AND place = ? AND event = ?",
            arrayOf(event.data, event.time, event.place, event.event))
        db.close()
    }

    fun deleteTask(task: Task) {
        val db = writableDatabase
        db.delete("tasks", "data = ? AND time = ? AND name = ? AND points = ? AND checkboxes = ?",
            arrayOf(task.data, task.time, task.name, task.points.joinToString(","), task.checkBoxes.joinToString(",")))
        db.close()
    }

    fun updateChosenDate(newDate: String) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("chosen_date", newDate)
        db.update("chosenDate", contentValues, "_id =?", arrayOf("1"))
        db.close()
    }

    fun updateTaskCheckBoxes(task: Task, checkBoxes: List<Boolean>) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("checkboxes", checkBoxes.joinToString(","))
        db.update("tasks", contentValues, "data = ? AND time = ? AND name = ? AND points = ?",
            arrayOf(task.data, task.time, task.name, task.points.joinToString(",")))
        db.close()
    }
}