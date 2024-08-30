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
        db.execSQL("CREATE TABLE user (login TEXT DEFAULT '', password TEXT DEFAULT '', own_room INTEGER DEFAULT 0, available_rooms TEXT DEFAULT '')")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS chosenDate")
        db.execSQL("DROP TABLE IF EXISTS roomId")
        db.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    fun createUser(user: User) {
        val db = writableDatabase
        val query = "INSERT INTO user (login, password, own_room, available_rooms) VALUES (?, ?, ?, ?)"
        db.execSQL(query, arrayOf(user.login, user.password, user.ownRoom, user.availableRooms))
    }

    fun getUser(): User? {
        var db = readableDatabase
        val query = "SELECT * FROM user"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val login = cursor.getString(0)
            val password = cursor.getString(1)
            val ownRoom = cursor.getLong(2)
            val availableRooms = cursor.getString(3)
            return User(login, password, ownRoom, availableRooms)
        }
        return null
    }

    fun updateUser(user: User) {
        val db = writableDatabase
        val query = "UPDATE user SET login = ?, password = ?, own_room = ?, available_rooms = ?"
        db.execSQL(query, arrayOf(user.login, user.password, user.ownRoom, user.availableRooms))
        db.close()
    }

    fun addRoomId(roomId :Int){
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("room_id", roomId.toString())
        db.insert("roomId", null, contentValues)
        db.close()
    }

    fun updateRoomId(roomId :Long) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put("room_id", roomId.toString())
        db.update("roomId", contentValues, "_id =?", arrayOf("1"))
        db.close()
    }

    fun getRoomId():Long{
        val db = readableDatabase
        val cursor = db.query("roomId", null, null, null, null, null, null)
        var roomId = -1
        if (cursor.moveToFirst()) {
            roomId = cursor.getString(1).toInt()
        }
        if(roomId == -1){
            addRoomId(-1)
        }
        cursor.close()
        db.close()
        return roomId.toLong()
    }

    fun getChosenDate(): String {
        var db = readableDatabase
        val cursor = db.query("chosenDate", null, null, null, null, null, null)
        val chosenDate: String

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