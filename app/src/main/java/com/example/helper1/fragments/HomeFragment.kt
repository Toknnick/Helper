package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.helper1.MainActivity
import com.example.helper1.R
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.CreateUserCallback
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.dataBase.Event
import com.example.helper1.dataBase.GetAllEventsCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetAllTaskCallback
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.GetUserCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.Task
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.EventManager
import com.example.helper1.dataBase.managers.RoomManager
import com.example.helper1.dataBase.managers.TaskManager
import com.example.helper1.dataBase.managers.UserManager
import com.example.helper1.databinding.FragmentHomeBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@SuppressLint("SetTextI18n","ClickableViewAccessibility")
@Suppress("DEPRECATION", "NAME_SHADOWING")
class HomeFragment : Fragment(){
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private lateinit var binding: FragmentHomeBinding
    private lateinit var userManager: UserManager
    private lateinit var roomManger: RoomManager
    private lateinit var eventManager: EventManager
    private lateinit var taskManager: TaskManager
    private lateinit var mainActivity: MainActivity
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var datePickerDialogForObject: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var deleteButton: Button
    private lateinit var editButton: Button
    private var user : User? = null

    private var chosenDate: String = ""
    private var events: List<Event> = ArrayList()
    private var tasks: List<Task> = ArrayList()
    private var countOfPoint = 1
    private var textSize = 21F

    private val ENENT_ID = 10000
    private val TASK_ID = 232320
    private val REL_LAYOUT_ID = 145632223
    private val TEXT_VIEW_NOTHING_TO_DO_ID = 11111111

    private lateinit var dbHelper: DBHelper
    private var idRoomDef: Long = 1

    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = (activity as MainActivity)
        addParamsToButtons(binding.point0)
        val apiClient = ApiClient(retrofit)
        userManager = UserManager(apiClient)
        roomManger = RoomManager(apiClient)
        eventManager = EventManager(apiClient)
        taskManager = TaskManager(apiClient)
        dbHelper = DBHelper(requireContext())



        //requireContext().deleteDatabase(dbHelper.databaseName)


        user = dbHelper.getUser()
        if(user == null){
            binding.addButton.visibility = View.GONE
            binding.dataPickerButton.visibility = View.GONE
            binding.createUserPanel.visibility = View.VISIBLE
            binding.saveUserButton.setOnClickListener{
                if(binding.passwordUser.text.toString().trim().isEmpty()){
                    createError("Пароль не может быть пустым")
                } else if(binding.passwordUser.text.toString().trim().length < 5){
                    createError("Минимальная длина пароля 6 символов")
                } else if(binding.loginUser.text.toString().trim().isEmpty()){
                    createError("Ошибка! Не указан логин пользователя")
                }
                else {
                    createUserForAPI()
                }
            }
        }else{
            loginUserForAPI(user!!,true)
            idRoomDef = user!!.ownRoom
        }
        initDatePicker()
        initTimePicker()

        binding.addButton.setOnClickListener {
            showDialog()
        }
        binding.addNewPoint.setOnClickListener {
            addNewPoint()
        }
        binding.deletePoint.setOnClickListener {
            deletePoint()
        }
        binding.dataPickerButton.setOnClickListener {
            datePickerDialog.show()
        }
        binding.backTaskButton.setOnClickListener {
            hideTaskPanel()
            clearTaskPanel()
        }
        binding.backEventButton.setOnClickListener {
            hideEventPanel()
            clearEventPanel()
        }
        binding.dateEvent.setOnClickListener {
            datePickerDialogForObject.show()
        }
        binding.dateTask.setOnClickListener {
            datePickerDialogForObject.show()
        }
        binding.timeEvent.setOnClickListener {
            timePickerDialog.show()
        }
        binding.timeTask.setOnClickListener {
            timePickerDialog.show()
        }

        binding.loginUserButton.setOnClickListener{
            var loggingUser = User(
                    binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim(),
            0,
            ""
            )
            if(loggingUser.login.isNotEmpty() && loggingUser.password.isNotEmpty()) {
                loginUserForAPI(loggingUser, false)
            }else{
                createError("Ошибка! Заполните данные")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chosenDate = dbHelper.getChosenDate()
        binding.dataPickerButton.text = chosenDate
        if (user != null) {
            changeScrollView()
        }
    }

    private fun loginUserForAPI(loggingUser : User, isUpdateUserData : Boolean){
        Log.d("MyTag","Зашел в метод")
        userManager.getUser(binding.loginUser.text.toString().trim(),object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                if(!isUpdateUserData) {
                    if (loggingUser.password == gotUser.password) {
                        dbHelper.createUser(loggingUser)
                        binding.createUserPanel.visibility = View.GONE
                    } else {
                        Toast.makeText(requireContext(), "Неверный пароль!", Toast.LENGTH_LONG)
                            .show()

                    }
                }else{
                    loggingUser.password = gotUser.password
                    loggingUser.availableRooms = gotUser.availableRooms
                    mainActivity.startActivity()
                    dbHelper.updateUser(loggingUser)
                }
            }


            override fun onFailure(isExist: Boolean) {
                if(!isUpdateUserData)
                    Toast.makeText(requireContext(),"Ошибка! Пользователь не найден", Toast.LENGTH_LONG).show()
            }
        })

        Log.d("MyTag",loggingUser.toString())
    }

    private fun createUserForAPI() {
        val newRoom = Room(
            0,
            binding.loginUser.text.toString().trim(),
            binding.passwordUser.text.toString().trim()
        )
        userManager.getUser(binding.loginUser.text.toString().trim(), object : GetUserCallback {
            override fun onSuccess(user: User) {
                //Проверка на уникальность логина
                Toast.makeText(requireContext(),"Ошибка! Такой логин уже существует!", Toast.LENGTH_LONG).show()
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
                dbHelper.createUser(user)
                mainActivity.startActivity()
                binding.createUserPanel.visibility = View.GONE
                binding.addButton.visibility = View.VISIBLE
                binding.dataPickerButton.visibility = View.VISIBLE
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserForAPI(newUser: User) {
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

    private fun updateRoomPasswordForAPI(newRoom: Room) {
        roomManger.updateRoom(newRoom, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRoomFromAPI(gettingRoom: Room) {
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


    private fun createEventForAPI(newEvent: Event) {
        eventManager.getAllEvents(idRoomDef, object : GetAllEventsCallback {
            override fun onSuccess(events: List<Event>) {
                newEvent.idEvent = (events.count()+1).toLong()
                eventManager.createEvent(newEvent, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        createAllEventsAndTasks()
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
    private fun getEventsByDateForAPI(){
        events = mutableListOf()
        eventManager.getAllEvents(idRoomDef, object : GetAllEventsCallback {
            override fun onSuccess(tempEvents: List<Event>) {
                for (event in tempEvents) {
                    if(event.date == chosenDate){
                        events += event
                    }
                }
                events.sortedBy { it.time }
                createAllEventsAndTasks()

            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateEventForAPI(previousEvent: Event, updatingEvent: Event){
        eventManager.updateEvent(previousEvent, updatingEvent, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                changeScrollView()
                checkToNothingToDo()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun deleteEventForAPI(deletingEvent: Event){
        eventManager.deleteEvent(deletingEvent,object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                changeScrollView()
                checkToNothingToDo()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun createTaskForAPI(newTask: Task) {
        taskManager.getAllTasks(idRoomDef, object : GetAllTaskCallback {
            override fun onSuccess(tasks: List<Task>) {
                newTask.idTask = (tasks.count()+1).toLong()
                taskManager.createTask(newTask, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        createAllEventsAndTasks()
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
    private fun getTasksByDateForAPI(){
        tasks = mutableListOf()
        taskManager.getAllTasks(idRoomDef, object : GetAllTaskCallback {
            override fun onSuccess(tempTasks: List<Task>) {
                for (task in tempTasks) {
                    if(task.date == chosenDate){
                        tasks += task
                    }
                }
                tasks.sortedBy { it.time }

                createAllEventsAndTasks()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateTaskForAPI(previousTask: Task, updatingTask: Task){
        taskManager.updateTask(previousTask, updatingTask, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                getTasksByDateForAPI()
                checkToNothingToDo()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteTaskForAPI(deletingTask: Task){
        taskManager.deleteTask(deletingTask,object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                changeScrollView()
                checkToNothingToDo()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }







    private fun createNewText(item: Int) {
        binding.saveEventButton.setOnClickListener {
            addNewEventIntoScrollView()
        }
        binding.saveTaskButton.setOnClickListener {
            addNewTaskIntoScrollView()
        }
        if (item == 0) {
            //Делаем задачу
            clearEventPanel()
            countOfPoint = 1
            binding.createTaskPanel.visibility = View.VISIBLE
            binding.addButton.isEnabled = false
        } else {
            //Делаем событие
            clearTaskPanel()
            binding.createEventPanel.visibility = View.VISIBLE
            binding.addButton.isEnabled = false
        }
    }

    private fun addNewEventIntoScrollView() {
        if (binding.eventEvent.text.toString().trim().isEmpty()) {
            createError("Ошибка! Нет описания!")
            return
        }

        //Сохраняем в БД
        val event = Event(
            0,
            idRoomDef,
            stringToDate(binding.dateEvent.text.toString()),
            stringToTime(binding.timeEvent.text.toString()),
            binding.placeEvent.text.toString(),
            binding.eventEvent.text.toString()
        )

        createError("Созданно на " + event.date)

        if (chosenDate == event.date) {
            events += event
        }

        hideEventPanel()
        clearEventPanel()
        createEventForAPI(event)
    }

    private fun addNewTaskIntoScrollView() {
        //Добавляем пункты
        var points: List<String> = ArrayList()
        var checkBoxes: List<Boolean> = ArrayList()
        if(binding.point0.text.trim().toString().isNotEmpty() && binding.point0.text.trim().toString()!=""){
            points += binding.point0.text.toString()
            checkBoxes += false
        }

        var j = 1
        while (countOfPoint > j) {
            if (binding.pointsPlace.findViewById<EditText>(j + TASK_ID).text.trim().toString()
                    .isNotEmpty()
            ) {
                points += binding.pointsPlace.findViewById<EditText>(j + TASK_ID).text.toString()
                    .trim()
                checkBoxes += false
            }
            j += 1
        }

        if (binding.timeTask.text.toString().trim().isEmpty()) {
            binding.timeTask.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
        }

        if(points.isNotEmpty()) {
            //Сохраняем в БД
            val newTask = Task(
                0,
                idRoomDef,
                stringToDate(binding.dateTask.text.toString().trim()),
                binding.timeTask.text.toString().trim(),
                binding.nameTask.text.toString().trim(),
                points.joinToString("|"),
                checkBoxes.map { it.toString() }.joinToString("|")
            )

            if (chosenDate == newTask.date) {
                tasks += newTask
            }

            createError("Созданно на " + newTask.date)
            createTaskForAPI(newTask)
        }
        else{
            createError("Задача не создана. Она была пуста")
        }

        hideTaskPanel()
        clearTaskPanel()
    }

    @SuppressLint("ResourceType")
    private fun createNewTask(task: Task) {
        if (binding.layout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            binding.layout.removeView(binding.layout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val layout = createRelativeLayout(tasks.indexOf(task))
        val nameTextView: TextView

        if (task.name.isEmpty())
            nameTextView = createText("Без названия", true)
        else
            nameTextView = createText(task.name, true)
        layout.addView(nameTextView)
        nameTextView.id = 666
        nameTextView.textSize = textSize + 1
        nameTextView.gravity = Gravity.CENTER

        val points: List<String> = task.points.splitToSequence("|").toList()
        val checkBoxes: List<Boolean> = task.checkBoxes.split("|").map { it.toBoolean() }

        if (points.count() > points.count()) {
            repairTask(task, points, checkBoxes)
        }

        var j = 0
        while (points.count() > j) {
            val textView = createText(points[j])
            addParamsToNewPoint(textView, layout, j, checkBoxes[j])
            j += 1
        }

        layout.setBackgroundResource(R.drawable.border_task)
        binding.layout.addView(layout)
        setupLongClickListeners(layout, tasks.indexOf(task))
    }

    private fun repairTask(task: Task, points:List<String>, checkBoxes : List<Boolean>) {
        var tempPoints: List<String> = ArrayList()
        var i = 0
        while (i < points.count() - 2) {
            tempPoints += points[i]
            i += 1
        }
        tempPoints += (points[points.count() - 2] + points[points.count() - 1])
        deleteTaskForAPI(task)
        task.points = tempPoints.joinToString("|")
        createTaskForAPI(task)
    }

    private fun createNewEvent(i: Int) {
        if (binding.layout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            binding.layout.removeView(binding.layout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val textView: TextView

        if (events[i].place.isNotEmpty() && events[i].time.length < 7) {
            textView = createText(
                "Время: " + events[i].time + System.lineSeparator() +
                        "Место: " + events[i].place + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else if (events[i].time.length < 7) {
            textView = createText(
                "Время: " + events[i].time + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else if (events[i].place.isNotEmpty() && events[i].event.isNotEmpty()) {
            textView = createText(
                "Место: " + events[i].place + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else {
            textView = createText(events[i].event, false, true)
        }

        textView.setBackgroundResource(R.drawable.border_event)
        textView.id = i + ENENT_ID
        binding.layout.addView(textView)
        setupLongClickListeners(textView, i)
    }

    private fun addNewPoint(text: String = "") {
        val editText = EditText(context)
        //Подвинуть пункт
        addParamsToEditText(editText,text)
        //Подвинуть кнопки
        addParamsToButtons(editText)
        countOfPoint += 1
    }

    //TODO: добавить возможность менять цветa в настройках
    @SuppressLint("ResourceAsColor")
    private fun createText(
        text: String,
        isNameText: Boolean = false,
        isNeedBelow: Boolean = false
    ): TextView {
        val textView = TextView(context)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(15, 15, 15, 15)

        if (isNameText) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            params.setMargins(1, 1, 1, 1)
        }

        if (isNeedBelow) {
            textView.id = 1488632223 + events.size
            if (binding.layout.childCount == 0 || (binding.layout.childCount == 1 && binding.layout.getChildAt(
                    0
                ) == deleteButton)
            ) {
                params.addRule(RelativeLayout.ALIGN_PARENT_START)
            } else if (binding.layout.getChildAt(binding.layout.childCount - 1) != deleteButton) {
                params.addRule(
                    RelativeLayout.BELOW,
                    binding.layout.getChildAt(binding.layout.childCount - 1).id
                )
            } else {
                params.addRule(
                    RelativeLayout.BELOW,
                    binding.layout.getChildAt(binding.layout.childCount - 2).id
                )
            }
        }
        textView.setTextColor(R.color.text_color)
        textView.setLayoutParams(params)
        textView.text = text
        textView.textSize = textSize
        return textView
    }

    @SuppressLint("ResourceAsColor")
    private fun createButton(text: String): Button {
        val button = Button(requireContext())
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)
        button.setLayoutParams(params)

        button.setBackgroundColor(resources.getColor(R.color.button_color))
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
        button.text = text
        button.textSize = textSize
        button.visibility = View.GONE
        return button
    }

    private fun createRelativeLayout(id: Int): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.id = REL_LAYOUT_ID + id
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)


        if (binding.layout.childCount == 0 || (binding.layout.childCount == 1 && binding.layout.getChildAt(
                0
            ) == deleteButton)
        ) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
        } else if (binding.layout.getChildAt(binding.layout.childCount - 1) != deleteButton) {
            params.addRule(
                RelativeLayout.BELOW,
                binding.layout.getChildAt(binding.layout.childCount - 1).id
            )
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                binding.layout.getChildAt(binding.layout.childCount - 2).id
            )
        }

        layout.setLayoutParams(params)
        layout.setPadding(0, 0, 0, 10)
        return layout
    }

    @SuppressLint("ResourceAsColor")
    private fun addParamsToEditText(editText: EditText, text: String = "") {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        //Установить новый пункт
        params.setMargins(30, 30, 30, 30)
        editText.setLayoutParams(params)
        editText.setBackgroundResource(R.color.edit_text)
        editText.hint = "Пункт"
        editText.setText(text)
        editText.id = countOfPoint + TASK_ID
        editText.setPadding(10, 10, 10, 40)
        binding.pointsPlace.addView(editText)
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))

        //Подвинуть новый пункт
        if (countOfPoint == 1) {
            params.addRule(RelativeLayout.BELOW, binding.point0.id)
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                binding.createTaskPanel.findViewById<EditText>(countOfPoint + TASK_ID - 1).id
            )
        }
        editText.setLayoutParams(params)
    }

    @SuppressLint("ResourceType")
    private fun addParamsToNewPoint(
        textView: TextView,
        relLayout: RelativeLayout,
        j: Int,
        isChecked: Boolean
    ) {
        val checkBox = CheckBox(context)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val checkBoxParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        if (j == 0) {
            checkBoxParams.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(666).id)
        } else {
            checkBoxParams.addRule(
                RelativeLayout.BELOW,
                relLayout.findViewById<TextView>(j + 1321210 - 1).id
            )
        }

        params.setMargins(0, 0, 0, 0)
        textView.maxWidth = (Resources.getSystem().displayMetrics.widthPixels * 0.9f).toInt()

        checkBox.isChecked = isChecked

        if (checkBox.isChecked)
            textView.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                text = textView.text
            }
        else
            textView.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                text = textView.text
            }

        checkBox.id = j + 121210
        textView.id = j + 1321210
        params.addRule(RelativeLayout.ALIGN_TOP, checkBox.id)
        params.addRule(RelativeLayout.RIGHT_OF, checkBox.id)
        checkBox.setLayoutParams(checkBoxParams)
        textView.setLayoutParams(params)

        checkBox.setOnClickListener {
            changeBackgroundOfPoint(textView, checkBox, relLayout.id - REL_LAYOUT_ID, j)
        }

        relLayout.addView(checkBox)
        relLayout.addView(textView)
    }

    private fun addParamsToButtons(editText: EditText) {
        val btn1Params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val btn2Params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val btn3Params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        btn1Params.addRule(RelativeLayout.ALIGN_START, editText.id)
        btn1Params.addRule(RelativeLayout.BELOW, editText.id)
        binding.saveTaskButton.setLayoutParams(btn1Params)

        btn2Params.addRule(RelativeLayout.ALIGN_END, editText.id)
        btn2Params.addRule(RelativeLayout.BELOW, editText.id)
        binding.addNewPoint.setLayoutParams(btn2Params)

        btn3Params.addRule(RelativeLayout.BELOW, editText.id)
        btn3Params.addRule(RelativeLayout.LEFT_OF, binding.addNewPoint.id)
        binding.deletePoint.setLayoutParams(btn3Params)
    }

    @SuppressLint("SetTextI18n")
    private fun changeBackgroundOfPoint(textView: TextView, checkBox: CheckBox, i: Int, j: Int) {
        val newCheckBoxes = tasks[i].checkBoxes.split("|").map { it.toBoolean() }.toMutableList()

        if (checkBox.isChecked) {
            textView.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                text = textView.text
            }
            newCheckBoxes[j] = true
        } else {
            textView.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                text = textView.text
            }
            newCheckBoxes[j] = false
        }
        val newTask = tasks[i]
        newTask.checkBoxes = newCheckBoxes.map { it.toString() }.joinToString("|")

        updateTaskForAPI(tasks[i],newTask)
    }

    private fun clearEventPanel() {
        binding.dateEvent.setText("")
        binding.timeEvent.setText("")
        binding.placeEvent.setText("")
        binding.eventEvent.setText("")
    }

    private fun clearTaskPanel() {
        binding.dateTask.setText("")
        binding.timeTask.setText("")
        binding.nameTask.setText("")
        binding.point0.setText("")
        addParamsToButtons(binding.point0)

        while (countOfPoint > 1) {
            binding.pointsPlace.removeView(
                binding.createTaskPanel.findViewById<EditText>(
                    countOfPoint + TASK_ID - 1
                )
            )
            countOfPoint -= 1
        }
    }

    private fun deletePoint() {
        if (countOfPoint > 2) {
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID - 1))
            countOfPoint -= 1
            addParamsToButtons(binding.pointsPlace.findViewById(countOfPoint + TASK_ID - 1))
        } else if (countOfPoint > 1) {
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID - 1))
            countOfPoint -= 1
            addParamsToButtons(binding.point0)
        } else {
            createError("Ошибка! Нельзя удалить этот пункт!")
        }
    }

    private fun createError(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun hideTaskPanel() {
        binding.addButton.isEnabled = true
        binding.createTaskPanel.visibility = View.GONE
    }

    private fun hideEventPanel() {
        binding.addButton.isEnabled = true
        binding.createEventPanel.visibility = View.GONE
    }

    private fun stringToDate(string: String): String {
        val newString: String

        if (string.length == 8)
            newString = string.replace(Regex("(\\d{2})$"), "20$1")
        else if (string.isEmpty())
            newString = chosenDate
        else
            newString = string

        return newString
    }

    private fun stringToTime(string: String): String {
        val newString: String
        if (string.isEmpty())
            newString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        else
            newString = string

        return newString
    }

    private fun setupLongClickListeners(view: View, id: Int) {
        view.setOnLongClickListener {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val paramsToEdit = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 5, 5, 5)
            paramsToEdit.setMargins(5, 5, 5, 5)

            if (binding.layout.indexOfChild(view) == 0) {
                params.addRule(RelativeLayout.BELOW, view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            } else {
                params.addRule(RelativeLayout.ABOVE, view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            }
            deleteButton.setLayoutParams(params)
            deleteButton.visibility = View.VISIBLE

            deleteButton.setOnClickListener {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                when (view) {
                    is TextView -> {
                        deleteEventForAPI(events[id])
                    }

                    is RelativeLayout -> {
                        deleteTaskForAPI(tasks[id])
                    }
                }
            }

            if (binding.layout.indexOfChild(view) == 0) {
                paramsToEdit.addRule(RelativeLayout.BELOW, view.id)
                paramsToEdit.addRule(RelativeLayout.ALIGN_RIGHT, view.id)
            } else {
                paramsToEdit.addRule(RelativeLayout.ABOVE, view.id)
                paramsToEdit.addRule(RelativeLayout.ALIGN_RIGHT, view.id)
            }
            editButton.setLayoutParams(paramsToEdit)
            editButton.visibility = View.VISIBLE

            editButton.setOnClickListener {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                when (view) {
                    is TextView -> {
                        editEvent(events[id])
                    }

                    is RelativeLayout -> {
                        editTask(tasks[id])
                    }
                }
            }
            true
        }

        setTouchListener(view)
        setTouchListener(binding.layout)
        setTouchListener(binding.conLayout)
    }

    private fun editEvent(event: Event) {
        binding.createEventPanel.visibility = View.VISIBLE
        binding.dateEvent.setText(event.date)
        if(event.time.length < 7){
            binding.timeEvent.setText(event.time)}
        binding.placeEvent.setText(event.place)
        binding.eventEvent.setText(event.event)
        binding.addButton.isEnabled = false
        binding.saveEventButton.setOnClickListener {

            if (binding.timeEvent.text.toString().isEmpty()) {
                binding.timeEvent.setText(event.time)
            }
            val newEvent = Event(
                0,
                idRoomDef,
                stringToDate(binding.dateEvent.text.toString()),
                stringToTime(binding.timeEvent.text.toString()),
                binding.placeEvent.text.toString(),
                binding.eventEvent.text.toString()
            )
            updateEventForAPI(event,newEvent)
            getEventsByDateForAPI()
            clearEventPanel()
            hideEventPanel()
        }
    }

    private fun editTask(task: Task) {
        binding.createTaskPanel.visibility = View.VISIBLE
        binding.dateTask.setText(task.date)
        if(task.time.length < 7){
            binding.timeTask.setText(task.time)}
        binding.nameTask.setText(task.name)
        val previousPoints: List<String> = task.points.splitToSequence("|").toMutableList()
        binding.point0.setText(previousPoints[0])
        addParamsToButtons(binding.point0)

        var i = 1
        while (previousPoints.count() > i) {
            addNewPoint(previousPoints[i])
            i+=1
        }

        binding.saveTaskButton.setOnClickListener {
            if(previousPoints.isNotEmpty()) {
                var points: List<String> = ArrayList()
                var checkBoxes: List<Boolean> = ArrayList()
                if(binding.point0.text.trim().toString().isNotEmpty()){
                    points += binding.point0.text.toString()
                    checkBoxes += false
                }

                var j = 1
                while (countOfPoint > j) {
                    if (binding.pointsPlace.findViewById<EditText>(j + TASK_ID).text.trim().toString()
                            .isNotEmpty()
                    ) {
                        points += binding.pointsPlace.findViewById<EditText>(j + TASK_ID).text.toString()
                            .trim()
                        checkBoxes += false
                    }
                    j += 1
                }

                if(binding.timeTask.text.toString().isEmpty()){
                    binding.timeTask.setText(task.time)}

                val newTask = Task(
                    0,
                    idRoomDef,
                    stringToDate(binding.dateTask.text.toString()),
                    stringToTime(binding.timeTask.text.toString()),
                    binding.nameTask.text.toString(),
                    points.joinToString("|"),
                    checkBoxes.map { it.toString() }.joinToString("|")
                )
                updateTaskForAPI(task,newTask)
                getTasksByDateForAPI()
            }else{
                createError("Ошибка! Задача была пуста")
            }
            clearTaskPanel()
            hideTaskPanel()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deleteButton.visibility = View.GONE
                editButton.visibility = View.GONE
            }
            false
        }
    }

    private fun createAllEventsAndTasks() {
        binding.layout.removeAllViews()
        deleteButton = createButton("Удалить")
        editButton = createButton("Редактировать")

        val newList = (events + tasks).sortedBy { it.time }
        for (item in newList) {
            when (item) {
                is Event -> createNewEvent(events.indexOf(item))
                is Task -> createNewTask(item)
            }
        }
        binding.layout.addView(deleteButton)
        binding.layout.addView(editButton)
        checkToNothingToDo()
    }

    private fun checkToNothingToDo() {
        if (binding.layout.childCount == 1) {
            val textView = createText("На этот день ничего не запланировано")
            textView.setTextColor(Color.GRAY)
            textView.id = TEXT_VIEW_NOTHING_TO_DO_ID
            binding.layout.addView(textView)
        }
    }

    private fun changeScrollView() {
        getEventsByDateForAPI()
        getTasksByDateForAPI()
    }

    private fun initDatePicker() {
        val dateSetListener = dateListener(true)
        val dateSetListenerForObject = dateListener(false)
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)

        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        datePickerDialog =
            DatePickerDialog(requireContext(), style, dateSetListener, year, month, day)
        datePickerDialogForObject =
            DatePickerDialog(requireContext(), style, dateSetListenerForObject, year, month, day)

    }

    private fun dateListener(isMainDatePicker: Boolean = false): DatePickerDialog.OnDateSetListener {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val date: String
            var month = month
            month += 1

            if (month > 9)
                date = "$day.$month.$year"
            else
                date = "$day.0$month.$year"

            if (isMainDatePicker) {
                binding.dataPickerButton.text = date
                chosenDate = date
                dbHelper.updateChosenDate(chosenDate)
                changeScrollView()
            } else {
                binding.dateEvent.setText(date)
                binding.dateTask.setText(date)
            }
        }
        return dateSetListener
    }

    private fun initTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val time: String
            if (hour > 9 && minute > 9)
                time = "$hour:$minute"
            else if (hour > 9)
                time = "$hour:0$minute"
            else if (minute > 9)
                time = "0$hour:$minute"
            else
                time = "0$hour:0$minute"

            binding.timeEvent.setText(time)
            binding.timeTask.setText(time)
        }

        val cal: Calendar = Calendar.getInstance()
        val hour: Int = cal.get(Calendar.HOUR_OF_DAY)
        val minute: Int = cal.get(Calendar.MINUTE)

        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        timePickerDialog =
            TimePickerDialog(requireContext(), style, timeSetListener, hour, minute, true)
    }

    private fun showDialog() {
        val langArray: Array<String> = arrayOf("Задача", "Событие")
        var selectedEvent = 0 // Инициализируем в 0, который является первым элементом
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Выберите тип")
        builder.setCancelable(false)

        builder.setSingleChoiceItems(
            langArray, 0
        ) { _, i ->
            selectedEvent = i
        }

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            createNewText(selectedEvent)
        }

        builder.setNegativeButton(
            "Отмена"
        ) { dialogInterface, _ -> // закрыть диалог
            dialogInterface.dismiss()
        }

        builder.show()
    }
}