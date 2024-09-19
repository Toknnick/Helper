package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.File
import com.example.helper1.dataBase.GetAllFilesCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileManager (private val apiClient: ApiClient) {
    fun createFile(file: File, callback: CreateMessageCallback) {
        apiClient.createFile(file, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

                } else {
                    callback.onFailure("Ошибка создания файла: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback.onFailure("Ошибка создания файла: ${t.localizedMessage}")
            }
        })
    }

    fun getAllFiles(callback: GetAllFilesCallback) {
        apiClient.getAllFiles(object : Callback<List<File>> {
            override fun onResponse(call: Call<List<File>>, response: Response<List<File>>) {
                if (response.isSuccessful) {
                    val files = response.body()
                    if (files != null) {
                        callback.onSuccess(files)
                    } else {
                        callback.onFailure("Ошибка получения файлов")
                    }
                } else {
                    callback.onFailure("Ошибка получения файлов")
                }
            }

            override fun onFailure(call: Call<List<File>>, t: Throwable) {
                callback.onFailure("Ошибка получения файлов")
            }
        })
    }

    fun getAllFilesByIdRoom(idRoom: Long, callback: GetAllFilesCallback) {
        apiClient.getAllFileByIdRoom(idRoom, object : Callback<List<File>> {
            override fun onResponse(call: Call<List<File>>, response: Response<List<File>>) {
                if (response.isSuccessful) {
                    val files = response.body()
                    if (files != null) {
                        callback.onSuccess(files)
                    } else {
                        callback.onFailure("Ошибка получения файлов")
                    }
                } else {
                    callback.onFailure("Ошибка получения файлов")
                }
            }

            override fun onFailure(call: Call<List<File>>, t: Throwable) {
                callback.onFailure("Ошибка получения файлов")
            }
        })
    }

    fun deleteFile(deletingFile: File, callback: CreateMessageCallback){
        apiClient.getFile(deletingFile,object : Callback<File> {
            override fun onResponse(call: Call<File>, response: Response<File>) {
                val foundedFile = response.body()
                if(foundedFile!=null) {
                    deletingFile.idFile= foundedFile.idFile
                }

                apiClient.deleteFile(deletingFile, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Удалил!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось удалить!")
                    }
                })
            }

            override fun onFailure(call: Call<File>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено фал")
            }
        })
    }
}