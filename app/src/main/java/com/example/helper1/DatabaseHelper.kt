package com.example.helper1

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
        db.execSQL("CREATE TABLE settings (_id INTEGER PRIMARY KEY, chosen_date TEXT DEFAULT '')")    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS settings")
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

    fun getChosenDate(): String {
        var db = readableDatabase
        val cursor = db.query("settings", null, null, null, null, null, null)
        var chosenDate = ""

        if (cursor.moveToFirst()) {
            chosenDate = cursor.getString(1)
        } else {
            db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put("chosen_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            db.insert("settings", null, contentValues)
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
        db.update("settings", contentValues, "_id =?", arrayOf("1"))
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