package com.example.helper1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mydatabase", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY, data TEXT, time TEXT, place TEXT, event TEXT)")
        db.execSQL("CREATE TABLE tasks (_id INTEGER PRIMARY KEY, data TEXT, time TEXT, name TEXT, points TEXT, checkboxes TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS tasks")
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

    fun updateTaskCheckBoxes(taskId: Int, checkBoxes: List<Boolean>) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("checkboxes", checkBoxes.joinToString(","))
        db.update("tasks", contentValues, "_id = ?", arrayOf(taskId.toString()))
        db.close()
    }
}