package com.example.helper1.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mydatabase", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE chosenDate (_id INTEGER PRIMARY KEY, chosen_date TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE roomId (_id INTEGER PRIMARY KEY, room_id TEXT DEFAULT '')")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS chosenDate")
        db.execSQL("DROP TABLE IF EXISTS roomId")
        onCreate(db)
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

    fun updateChosenDate(newDate: String) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("chosen_date", newDate)
        db.update("chosenDate", contentValues, "_id =?", arrayOf("1"))
        db.close()
    }
}