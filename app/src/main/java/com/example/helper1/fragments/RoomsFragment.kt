package com.example.helper1.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.CreateUserCallback
import com.example.helper1.dataBase.Event
import com.example.helper1.dataBase.GetAllEventsCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetAllTaskCallback
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.IsExistUserCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.Task
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.EventManager
import com.example.helper1.dataBase.managers.RoomManager
import com.example.helper1.dataBase.managers.TaskManager
import com.example.helper1.dataBase.managers.UserManager
import com.example.helper1.databinding.FragmentRoomsBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomsFragment : Fragment() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private lateinit var binding: FragmentRoomsBinding
    private lateinit var userManager: UserManager
    private lateinit var roomManger: RoomManager
    private lateinit var eventManager: EventManager
    private lateinit var taskManager: TaskManager


    private var chosenDate: String = "28.08.2024"

    private var idRoomDef: Long = 1

    //TODO: менять у нынешнего пользователя availableRooms после подключения к комнате
    //TODO: перенести метод с добавлением пользователя в homeFragment
    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment


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
        userManager = UserManager(apiClient)
        roomManger = RoomManager(apiClient)
        eventManager = EventManager(apiClient)
        taskManager = TaskManager(apiClient)
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
            createRoomForAPI(newRoom, false)
        }
        binding.updateRoomButton.setOnClickListener {
            updateRoomPasswordForAPI()
        }

        binding.saveEventButton.setOnClickListener {
            createEventForAPI()
        }
        binding.updateEventButton.setOnClickListener {
            deleteEventForAPI()
        }

        binding.saveTaskButton.setOnClickListener {
            getTaskByDateForAPI()
        }
    }

    private fun createUserForAPI() {
        val newRoom = Room(
            0,
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim()
        )
        userManager.getUser(
            binding.loginUser.text.toString().trim(),
            object : IsExistUserCallback {
                override fun onSuccess(isExist: Boolean) {
                    //Проверка на уникальность логина
                    Toast.makeText(
                        requireContext(),
                        "Ошибка! Такой логин уже существует!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onFailure(isExist: Boolean) {
                    //Создаем личную комнату под юзера
                    createRoomForAPI(newRoom, true)
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
        userManager.createUser(newUser, object : CreateUserCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onUserCreated(user: User) {
                //TODO: передается куда-то пользователь
                //val user = user
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserPasswordForAPI() {
        //TODO:поменять editTexts на строках ниже
        val newUser = User(
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim(),
            0,
            ""
        )
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, false)
    }


    private fun createRoomForAPI(newRoom: Room, isNewUser: Boolean) {
        roomManger.getAllRooms(object : GetAllRoomsCallback {
            override fun onSuccess(rooms: List<Room>) {
                val idRoom = (rooms.count() + 1).toLong()
                newRoom.idRoom = idRoom
                roomManger.createRoom(newRoom, object : CreateRoomCallback {
                    override fun onSuccess(message: String) {
                        if (!isNewUser)
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(message: String) {
                        if (!isNewUser)
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
        roomManger.updateRoom(newRoom, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRoomFromAPI() {
        //TODO:поменять editTexts на строках ниже
        val gettingRoom = Room(
            binding.nameRoom.text.toString().toLong(),
            "",
            binding.passwordRoom.text.toString().trim()
        )

        roomManger.getRoom(gettingRoom.idRoom, object : GetRoomCallback {
            override fun onSuccess(gotRoom: Room) {
                if (gettingRoom.password == gotRoom.password) {
                    //TODO: тут все норм, добавить на панель
                    Toast.makeText(requireContext(), "Комнату нашел!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка! Данные не верны!", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun createEventForAPI() {
        eventManager.getAllEvents(idRoomDef, object : GetAllEventsCallback {
            override fun onSuccess(events: List<Event>) {
                val newEvent = Event(
                    (events.count()+1).toLong(),
                    idRoomDef,
                    binding.dateEvent.text.toString().trim(),
                    binding.timeEvent.text.toString().trim(),
                    binding.placeEvent.text.toString().trim(),
                    binding.eventEvent.text.toString().trim()
                )
                eventManager.createEvent(newEvent, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        //TODO: пересобрать
                    }

                    override fun onFailure(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    private fun getEventsByDateForAPI():List<Event>{
        var events: List<Event> = ArrayList()
        eventManager.getAllEvents(idRoomDef, object : GetAllEventsCallback {
            override fun onSuccess(tempEvents: List<Event>) {
                for (event in tempEvents) {
                    if(event.date == chosenDate){
                        events += event
                    }
                }

            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })

        return events
    }

    private fun updateEventForAPI(previousEvent: Event){
        val updatingEvent = Event(
            0,
            idRoomDef,
            binding.dateEvent.text.toString().trim(),
            binding.timeEvent.text.toString().trim(),
            binding.placeEvent.text.toString().trim(),
            binding.eventEvent.text.toString().trim()
        )

        eventManager.updateEvent(previousEvent, updatingEvent, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                //TODO: пересобрать
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun deleteEventForAPI(){
        val deletingEvent = Event(
            0,
            idRoomDef,
            binding.dateEvent.text.toString().trim(),
            binding.timeEvent.text.toString().trim(),
            binding.placeEvent.text.toString().trim(),
            binding.eventEvent.text.toString().trim()
        )

        eventManager.deleteEvent(deletingEvent,object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                //TODO: пересобрать
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun createTaskForAPI() {
        taskManager.getAllTasks(idRoomDef, object : GetAllTaskCallback {
            override fun onSuccess(tasks: List<Task>) {
                //TODO: сделать получение из всех поинтов
                //TODO: добавлять запятые!!!!
                var points = ""
                points += binding.point0.text.toString()
                var checkBoxes = ""
                checkBoxes += "false"
                val newTask = Task(
                    (tasks.count()+1).toLong(),
                    idRoomDef,
                    binding.dateTask.text.toString().trim(),
                    binding.timeTask.text.toString().trim(),
                    binding.nameTask.text.toString().trim(),
                    points,
                    checkBoxes
                )
                taskManager.createTask(newTask, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        //TODO: пересобрать
                    }

                    override fun onFailure(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    private fun getTaskByDateForAPI():List<Task>{
        var tasks: List<Task> = ArrayList()
        taskManager.getAllTasks(idRoomDef, object : GetAllTaskCallback {
            override fun onSuccess(tempTasks: List<Task>) {
                for (task in tempTasks) {
                    if(task.date == chosenDate){
                        tasks += task
                    }
                }
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })

        return tasks
    }

    private fun updateTaskForAPI(previousTask: Task){
        //TODO: почини points и checkboxes
        val updatingTask = Task(
            0,
            idRoomDef,
            binding.dateTask.text.toString().trim(),
            binding.timeTask.text.toString().trim(),
            binding.nameTask.text.toString().trim(),
            binding.point0.text.toString().trim(),
            "false"
        )

        taskManager.updateTask(previousTask, updatingTask, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                //TODO: пересобрать
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteTaskForAPI(){
        //TODO: почини points и checkboxes

        val deletingTask = Task(
            0,
            idRoomDef,
            binding.dateTask.text.toString().trim(),
            binding.timeTask.text.toString().trim(),
            binding.nameTask.text.toString().trim(),
            binding.point0.text.toString().trim(),
            "false"
        )

        taskManager.deleteTask(deletingTask,object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                //TODO: пересобрать
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }



    //TODO: убрать потом сообщения об успехе

    //TODO: добавить строки ниже потом для даты и времени для ивента и таск
    //android:editable="false"
    //android:focusable="false"
    //android:inputType="date"
}