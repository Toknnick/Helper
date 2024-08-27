package com.example.helper1.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.MySQLController
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.User
import com.example.helper1.databinding.FragmentRoomsBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomsFragment : Fragment() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private lateinit var binding: FragmentRoomsBinding
    private lateinit var mysqlController: MySQLController

    private var idRoomDef: Long = 1

    //TODO: менять у нынешнего пользователя availableRooms после подключения к комнате
    //TODO: перенести методы с добавлением пользователя в homeFragment
    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment


    //TODO: сделать методы "update" для "event" и "task" в API

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiClient = ApiClient(retrofit)
        mysqlController = MySQLController(apiClient)
        binding.saveUserButton.setOnClickListener {
            createUserForAPI()
        }
        binding.updateUserButton.setOnClickListener {
            updateUserPasswordForAPI()
        }
        binding.saveRoomButton.setOnClickListener {
            val newRoom = Room(
                0,
                binding.nameRoom.text.toString().trim(),
                binding.passwordRoom.text.toString().trim()
            )
            createRoomForAPI(newRoom,false)
        }
        binding.updateRoomButton.setOnClickListener {
            updateRoomPasswordForAPI()
        }
    }

    private fun createUserForAPI() {
        val newRoom = Room(
            0,
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim()
        )
        mysqlController.getUser(binding.loginUser.text.toString().trim(), object : MySQLController.CreateIsExistUserCallback {
            override fun onSuccess(isExist: Boolean) {
                //Проверка на уникальность логина
                Toast.makeText(requireContext(), "Ошибка! Такой логин уже существует!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(isExist: Boolean) {
                //Создаем личную комнату под юзера
                createRoomForAPI(newRoom,true)
            }
        })
    }


    private fun createUser(idRoom: Long) {
        val newUser = User(
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim(),
            idRoom,
            ""
        )
        mysqlController.createUser(newUser, object : MySQLController.CreateUserCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onUserCreated(user: User) {
                //TODO: передается куда-то пользователь
                val user = user
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun updateUserPasswordForAPI() {
        val newUser = User(
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim(),
            0,
            ""
        )
        mysqlController.updateUser(newUser, object : MySQLController.CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, false)
    }

    private fun createRoomForAPI(newRoom: Room, isNewUser: Boolean) {
        mysqlController.getAllRooms(object : MySQLController.GetAllRoomsCallback {
            override fun onSuccess(rooms: List<Room>) {
                val idRoom = (rooms.count() + 1).toLong()
                newRoom.idRoom = idRoom
                mysqlController.createRoom(newRoom, object : MySQLController.CreateRoomCallback {
                    override fun onSuccess(message: String) {
                        if(!isNewUser)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(message: String) {
                        if(!isNewUser)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }

                    override fun onRoomCreated(idRoom: Long) {
                        idRoomDef = idRoom
                        Log.d("MyTag", "Комната создана с id $idRoom")
                        if (isNewUser) {
                            createUser(idRoomDef)
                        }
                    }
                })
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateRoomPasswordForAPI() {
        //TODO:поменять binding
        val newRoom = Room(
            binding.nameRoom.text.toString().toLong(),
            "",
            binding.passwordRoom.text.toString().trim()
        )
        mysqlController.updateRoom(newRoom, object : MySQLController.CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

}