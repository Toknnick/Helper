package com.example.helper1.dataBase

import retrofit2.Callback
import retrofit2.Retrofit

class ApiClient(private val retrofit: Retrofit) {
    private val apiInterface: ApiInterface = retrofit.create(ApiInterface::class.java)

    fun createUser(user: User, callback: Callback<User>) {
        val call = apiInterface.createUser(user)
        call.enqueue(callback)
    }

    fun getUserByLogin(login: String, callback: Callback<User>) {
        val call = apiInterface.getUserByLogin(login)
        call.enqueue(callback)
    }

    fun updateUser(user: User, callback: Callback<Void>) {
        val call = apiInterface.updateUser(user)
        call.enqueue(callback)
    }


    fun createRoom(room: Room, callback: Callback<Room>) {
        val call = apiInterface.createRoom(room)
        call.enqueue(callback)
    }

    fun getAllRooms(callback: Callback<List<Room>>)  {
        val call = apiInterface.getAllRooms()
        call.enqueue(callback)
    }

    fun getRoom(idRoom: Int, callback: Callback<Room>) {
        val call = apiInterface.getRoom(idRoom)
        call.enqueue(callback)
    }

    fun updateRoom(room: Room, callback: Callback<Void>) {
        val call = apiInterface.updateRoom(room)
        call.enqueue(callback)
    }


    fun createEvent(event: Event, callback: Callback<Event>) {
        val call = apiInterface.createEvent(event)
        call.enqueue(callback)
    }

    fun getAllEvents(callback: Callback<List<Event>>)  {
        val call = apiInterface.getAllEvent()
        call.enqueue(callback)
    }

    fun getAllEventsByIdRoom(idRoom: Int ,callback: Callback<List<Event>>) {
        val call = apiInterface.getAllEventsByIdRoom(idRoom)
        call.enqueue(callback)
    }

    fun getEvent(event: Event, callback: Callback<Event>) {
        val call = apiInterface.getEvent(event.idRoom,event.date,event.time,event.place,event.event)
        call.enqueue(callback)
    }

    fun updateEvent(event: Event, callback: Callback<Void>) {
        val call = apiInterface.updateEvent(event)
        call.enqueue(callback)
    }

    fun deleteEvent(event: Event, callback: Callback<Void>) {
        val call = apiInterface.deleteEvent(event.idEvent)
        call.enqueue(callback)
    }


    fun createTask(task: Task, callback: Callback<Task>) {
        val call = apiInterface.createTask(task)
        call.enqueue(callback)
    }

    fun getAllTasks(callback: Callback<List<Task>>)  {
        val call = apiInterface.getAllTasks()
        call.enqueue(callback)
    }

    fun getAllTasksByIdRoom(idRoom: Int ,callback: Callback<List<Task>>) {
        val call = apiInterface.getAllTasksByIdRoom(idRoom)
        call.enqueue(callback)
    }

    fun getTask(task: Task, callback: Callback<Task>) {
        val call = apiInterface.getTask(task.idRoom,task.date,task.time,task.name,task.points,task.checkBoxes)
        call.enqueue(callback)
    }

    fun updateTask(task: Task, callback: Callback<Void>) {
        val call = apiInterface.updateTask(task)
        call.enqueue(callback)
    }

    fun deleteTask(task: Task, callback: Callback<Void>) {
        val call = apiInterface.deleteTask(task.idTask)
        call.enqueue(callback)
    }

    fun createImage(image: Image,
                    callback: Callback<Void>) {
        val call = apiInterface.createImage(image)
        call.enqueue(callback)
    }

    fun getAllImages(callback: Callback<List<Image>>)  {
        val call = apiInterface.getAllImage()
        call.enqueue(callback)
    }

    fun getAllImageByIdRoom(idRoom: Int ,callback: Callback<List<Image>>) {
        val call = apiInterface.getAllImageByIdRoom(idRoom)
        call.enqueue(callback)
    }

    fun getImage(image: Image, callback: Callback<Image>) {
        val call = apiInterface.getImage(image.idRoom,image.date,image.time,image.url)
        call.enqueue(callback)
    }
    fun deleteImage(image: Image, callback: Callback<Void>) {
        val call = apiInterface.deleteImage(image.idImage)
        call.enqueue(callback)
    }


    fun createFile(file: File,
                    callback: Callback<Void>) {
        val call = apiInterface.createFile(file)
        call.enqueue(callback)
    }

    fun getAllFiles(callback: Callback<List<File>>)  {
        val call = apiInterface.getAllFile()
        call.enqueue(callback)
    }

    fun getAllFileByIdRoom(idRoom: Int ,callback: Callback<List<File>>) {
        val call = apiInterface.getAllFileByIdRoom(idRoom)
        call.enqueue(callback)
    }

    fun getFile(file: File, callback: Callback<File>) {
        val call = apiInterface.getFile(file.idRoom,file.date,file.time,file.url)
        call.enqueue(callback)
    }
    fun deleteFile(file: File, callback: Callback<Void>) {
        val call = apiInterface.deleteFile(file.idFile)
        call.enqueue(callback)
    }
}