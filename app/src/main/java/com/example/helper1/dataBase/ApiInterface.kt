package com.example.helper1.dataBase

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterface {
    @POST("users/create")
    fun createUser(@Body user: User): Call<User>

    @GET("users/get/{login}")
    fun getUserByLogin(@Path("login") login: String): Call<User>

    @PUT("users/update")
    fun updateUser(@Body user: User): Call<Void>


    @POST("rooms/create")
    fun createRoom(@Body room: Room): Call<Room>

    @GET("rooms/get/{idRoom}")
    fun getRoom(@Path("idRoom") idRoom: Long): Call<Room>

    @GET("rooms/get/all")
    fun getAllRooms(): Call<List<Room>>

    @PUT("rooms/update")
    fun updateRoom(@Body room: Room): Call<Void>


    @POST("events/create")
    fun createEvent(@Body event: Event): Call<Event>

    @GET("events/get/one/{idRoom}/{date}/{time}/{place}/{event}")
    fun getEvent(@Path("idRoom") idRoom:Long, @Path("date") date:String, @Path("time") time:String,
                 @Path("place") place:String, @Path("event") event:String): Call<Event>

    @GET("events/get/all")
    fun getAllEvent(): Call<List<Event>>

    @GET("events/get/all/{idRoom}")
    fun getAllEventsByIdRoom(@Path("idRoom") idRoom:Long): Call<List<Event>>

    @PUT("events/update")
    fun updateEvent(@Body event: Event): Call<Void>

    @DELETE("events/delete/{idEvent}")
    fun deleteEvent(@Path("idEvent") idEvent:Long): Call<Void>


    @POST("tasks/create")
    fun createTask(@Body task: Task): Call<Task>

    @GET("tasks/get/one/{idRoom}/{date}/{time}/{name}/{points}/{checkBoxes}")
    fun getTask(@Path("idRoom") idRoom:Long, @Path("date") date:String, @Path("time") time:String,
                 @Path("name") name:String, @Path("points") points:String, @Path("checkBoxes") checkBoxes:String): Call<Task>

    @GET("tasks/get/all")
    fun getAllTasks(): Call<List<Task>>

    @GET("tasks/get/all/{idRoom}")
    fun getAllTasksByIdRoom(@Path("idRoom") idRoom:Long): Call<List<Task>>

    @PUT("tasks/update")
    fun updateTask(@Body task: Task): Call<Void>

    @DELETE("tasks/delete/{idTask}")
    fun deleteTask(@Path("idTask") idTask:Long): Call<Void>


    @POST("images/create")
    fun createImage(@Body image: Image): Call<Void>

    @GET("images/get/one/{idRoom}/{date}/{time}/{url}")
    fun getImage(@Path("idRoom") idRoom:Long, @Path("date") date:String, @Path("time") time:String, @Path("url") url:String): Call<Image>

    @GET("images/get/all")
    fun getAllImage(): Call<List<Image>>

    @GET("images/get/all/{idRoom}")
    fun getAllImageByIdRoom(@Path("idRoom") idRoom:Long): Call<List<Image>>


    @DELETE("images/delete/{idImage}")
    fun deleteImage(@Path("idImage") idImage:Long): Call<Void>

}