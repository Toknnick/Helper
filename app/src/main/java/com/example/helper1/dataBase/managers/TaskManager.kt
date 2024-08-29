package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.GetAllTaskCallback
import com.example.helper1.dataBase.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskManager(private val apiClient: ApiClient) {
    fun createTask(task: Task, callback: CreateMessageCallback) {
        apiClient.createTask(task, object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                val createdTask = response.body()

                if (createdTask != null) {
                    callback.onSuccess("Успешно")
                } else {
                    callback.onFailure("Ошибка1 создания задачи")
                }
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                callback.onFailure("Ошибка2 создания задачи")
            }
        })
    }



    fun getAllTasks(idRoom: Long, callback: GetAllTaskCallback) {
        apiClient.getAllTasks(idRoom, object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    val tasks = response.body()
                    if (tasks != null) {
                        callback.onSuccess(tasks)
                    } else {
                        callback.onFailure("Ошибка1 получения задач")
                    }
                } else {
                    callback.onFailure("Ошибка2 получения задач")
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                callback.onFailure("Ошибка3 получения задач")
            }
        })
    }

    fun updateTask(previousTask: Task, updatingTask: Task, callback: CreateMessageCallback){
        updatingTask.idTask = previousTask.idTask
        apiClient.updateTask(updatingTask,object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                callback.onSuccess("Успех!")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback.onFailure("Ошибка! Не удалось обновить!")
            }
        })
    }

    fun deleteTask(deletingTask: Task, callback: CreateMessageCallback){
        apiClient.getTask(deletingTask,object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                val foundedTask = response.body()
                if(foundedTask!=null) {
                    deletingTask.idTask = foundedTask.idTask
                }

                apiClient.deleteTask(deletingTask, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Удалил!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось обновить!")
                    }
                })
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено событие")
            }
        })
    }
}