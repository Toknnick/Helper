package com.example.helper1.dataBase.managers

import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.GetAllImagesCallback
import com.example.helper1.dataBase.Image
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageManager(private val apiClient: ApiClient) {
    fun createImage(image:Image, callback: CreateMessageCallback) {
        apiClient.createImage(image, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

                } else {
                    callback.onFailure("Ошибка создания изображения: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback.onFailure("Ошибка создания изображения: ${t.localizedMessage}")
            }
        })
    }

    fun getAllImages(callback: GetAllImagesCallback) {
        apiClient.getAllImages(object : Callback<List<Image>> {
            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                if (response.isSuccessful) {
                    val images = response.body()
                    if (images != null) {
                        callback.onSuccess(images)
                    } else {
                        callback.onFailure("Ошибка получения изображений")
                    }
                } else {
                    callback.onFailure("Ошибка получения изображений")
                }
            }

            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                callback.onFailure("Ошибка получения изображений")
            }
        })
    }

    fun getAllImagesByIdRoom(idRoom: Int, callback: GetAllImagesCallback) {
        apiClient.getAllImageByIdRoom(idRoom, object : Callback<List<Image>> {
            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                if (response.isSuccessful) {
                    val images = response.body()
                    if (images != null) {
                        callback.onSuccess(images)
                    } else {
                        callback.onFailure("Ошибка получения изображений")
                    }
                } else {
                    callback.onFailure("Ошибка получения изображений")
                }
            }

            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                callback.onFailure("Ошибка получения изображений")
            }
        })
    }

    fun deleteImage(deletingImage: Image, callback: CreateMessageCallback){
        apiClient.getImage(deletingImage,object : Callback<Image> {
            override fun onResponse(call: Call<Image>, response: Response<Image>) {
                val foundedImage = response.body()
                if(foundedImage!=null) {
                    deletingImage.idImage= foundedImage.idImage
                }

                apiClient.deleteImage(deletingImage, object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        callback.onSuccess("Удалил!")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        callback.onFailure("Ошибка! Не удалось удалить!")
                    }
                })
            }

            override fun onFailure(call: Call<Image>, t: Throwable) {
                callback.onFailure("Ошибка! Не найдено изображение")
            }
        })
    }
}