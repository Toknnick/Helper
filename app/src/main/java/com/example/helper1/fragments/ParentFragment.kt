package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.helper1.MainActivity
import com.example.helper1.R
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.dataBase.Event
import com.example.helper1.dataBase.GetAllEventsCallback
import com.example.helper1.dataBase.GetAllImagesCallback
import com.example.helper1.dataBase.GetAllTaskCallback
import com.example.helper1.dataBase.Image
import com.example.helper1.dataBase.Task
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.EventManager
import com.example.helper1.dataBase.managers.ImageManager
import com.example.helper1.dataBase.managers.RoomManager
import com.example.helper1.dataBase.managers.TaskManager
import com.example.helper1.dataBase.managers.UserManager
import com.squareup.picasso.Picasso
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Suppress("DEPRECATION")
open class ParentFragment : Fragment() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    protected lateinit var userManager: UserManager
    protected lateinit var roomManger: RoomManager
    protected lateinit var eventManager: EventManager
    protected lateinit var taskManager: TaskManager
    protected lateinit var imageManager: ImageManager
    protected lateinit var mainActivity: MainActivity
    protected lateinit var datePickerDialog: DatePickerDialog
    protected lateinit var datePickerDialogForObject: DatePickerDialog
    protected lateinit var timePickerDialog: TimePickerDialog
    protected lateinit var deleteButton: Button
    protected lateinit var editButton: Button

    protected var user: User? = null

    protected var selectedImageUri: Uri? = null

    protected var chosenDate: String = ""
    protected var events: MutableList<Event> = ArrayList<Event>().toMutableList()
    protected var tasks: MutableList<Task> = ArrayList<Task>().toMutableList()
    protected var images: MutableList<Image> = ArrayList<Image>().toMutableList()
    private var countOfPoint = 1
    protected var textSize = 21F

    private val ENENT_ID = 10000
    private val TASK_ID = 232320
    private val REL_LAYOUT_ID = 145632223
    private val TEXT_VIEW_NOTHING_TO_DO_ID = 11111111

    protected lateinit var dbHelper: DBHelper


    protected lateinit var mainLayout: RelativeLayout
    protected lateinit var createRoomButton: Button
    protected lateinit var addRoomButton: Button
    protected lateinit var addButton: Button
    protected lateinit var dataPickerButton: Button
    protected lateinit var createTaskPanel: RelativeLayout
    protected lateinit var backTaskButton: Button
    protected lateinit var dateTask: EditText
    protected lateinit var timeTask: EditText
    protected lateinit var nameTask: EditText
    protected lateinit var point0: EditText
    protected lateinit var pointsPlace: RelativeLayout
    protected lateinit var addNewPoint: Button
    protected lateinit var deletePoint: Button
    protected lateinit var saveTaskButton: Button
    protected lateinit var createEventPanel: RelativeLayout
    protected lateinit var backEventButton: Button
    protected lateinit var dateEvent: EditText
    protected lateinit var timeEvent: EditText
    protected lateinit var placeEvent: EditText
    protected lateinit var eventEvent: EditText
    protected lateinit var saveEventButton: Button
    protected lateinit var taskTextView: TextView
    protected lateinit var eventTextView: TextView

    protected lateinit var roomNameTextView: TextView
    protected lateinit var showRoomPanelButton: LinearLayout
    protected lateinit var createRoomPanel: RelativeLayout
    protected lateinit var addRoomPanel: RelativeLayout
    protected lateinit var saveRoomButton: Button
    protected lateinit var backCreateRoomButton: Button
    protected lateinit var createNameRoom: EditText
    protected lateinit var createPasswordRoom: EditText
    protected lateinit var getRoomButton: Button
    protected lateinit var backAddRoomButton: Button
    protected lateinit var addIdRoom: EditText
    protected lateinit var addPasswordRoom: EditText
    protected lateinit var roomLayout: LinearLayout
    protected lateinit var showRoomPanel: RelativeLayout


    protected lateinit var createUserPanel: RelativeLayout
    protected lateinit var saveUserButton: Button
    protected lateinit var passwordUser: EditText
    protected lateinit var loginUser: EditText
    protected lateinit var loginUserButton: Button


    protected lateinit var createImagePanel: RelativeLayout
    protected lateinit var imageTextView: TextView
    protected lateinit var backImageButton: Button
    protected lateinit var dateImage: EditText
    protected lateinit var timeImage: EditText
    protected lateinit var chooseImageButton: Button
    protected lateinit var saveImageButton: Button
    protected lateinit var imageIcon: ImageView
    protected lateinit var calendarView: CalendarView
    protected lateinit var mainCalendarView: CalendarView


    protected var idRoomDef: Long = -1

    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment

    private fun initDefElements() {
        mainLayout = requireView().findViewById<RelativeLayout>(R.id.mainLayout)
        createRoomButton = requireView().findViewById<Button>(R.id.createRoomButton)
        addRoomButton = requireView().findViewById<Button>(R.id.addRoomButton)
        addButton = requireView().findViewById<Button>(R.id.addButton)
        dataPickerButton = requireView().findViewById<Button>(R.id.dataPickerButton)
        createTaskPanel = requireView().findViewById<RelativeLayout>(R.id.createTaskPanel)
        backTaskButton = requireView().findViewById<Button>(R.id.backTaskButton)
        dateTask = requireView().findViewById<EditText>(R.id.dateTask)
        timeTask = requireView().findViewById<EditText>(R.id.timeTask)
        nameTask = requireView().findViewById<EditText>(R.id.nameTask)
        point0 = requireView().findViewById<EditText>(R.id.point0)
        pointsPlace = requireView().findViewById<RelativeLayout>(R.id.pointsPlace)
        addNewPoint = requireView().findViewById<Button>(R.id.addNewPoint)
        deletePoint = requireView().findViewById<Button>(R.id.deletePoint)
        saveTaskButton = requireView().findViewById<Button>(R.id.saveTaskButton)
        createEventPanel = requireView().findViewById<RelativeLayout>(R.id.createEventPanel)
        backEventButton = requireView().findViewById<Button>(R.id.backEventButton)
        dateEvent = requireView().findViewById<EditText>(R.id.dateEvent)
        timeEvent = requireView().findViewById<EditText>(R.id.timeEvent)
        placeEvent = requireView().findViewById<EditText>(R.id.placeEvent)
        eventEvent = requireView().findViewById<EditText>(R.id.eventEvent)
        saveEventButton = requireView().findViewById<Button>(R.id.saveEventButton)
        taskTextView = requireView().findViewById<TextView>(R.id.taskTextView)
        eventTextView = requireView().findViewById<TextView>(R.id.eventTextView)

        roomNameTextView = requireView().findViewById<TextView>(R.id.roomNameTextView)
        showRoomPanelButton = requireView().findViewById<LinearLayout>(R.id.showRoomPanelButton)
        createRoomPanel = requireView().findViewById<RelativeLayout>(R.id.createRoomPanel)
        addRoomPanel = requireView().findViewById<RelativeLayout>(R.id.addRoomPanel)
        saveRoomButton = requireView().findViewById<Button>(R.id.saveRoomButton)
        backCreateRoomButton = requireView().findViewById<Button>(R.id.backCreateRoomButton)
        createNameRoom = requireView().findViewById<EditText>(R.id.createNameRoom)
        createPasswordRoom = requireView().findViewById<EditText>(R.id.createPasswordRoom)
        getRoomButton = requireView().findViewById<Button>(R.id.getRoomButton)
        backAddRoomButton = requireView().findViewById<Button>(R.id.backAddRoomButton)
        addIdRoom = requireView().findViewById<EditText>(R.id.addIdRoom)
        addPasswordRoom = requireView().findViewById<EditText>(R.id.addPasswordRoom)
        roomLayout = requireView().findViewById<LinearLayout>(R.id.roomLayout)
        showRoomPanel = requireView().findViewById<RelativeLayout>(R.id.showRoomPanel)

        createUserPanel = requireView().findViewById<RelativeLayout>(R.id.createUserPanel)
        saveUserButton = requireView().findViewById<Button>(R.id.saveUserButton)
        passwordUser = requireView().findViewById<EditText>(R.id.passwordUser)
        loginUser = requireView().findViewById<EditText>(R.id.loginUser)
        loginUserButton = requireView().findViewById<Button>(R.id.loginUserButton)

        createImagePanel = requireView().findViewById<RelativeLayout>(R.id.createImagePanel)
        imageTextView = requireView().findViewById<TextView>(R.id.imageTextView)
        backImageButton = requireView().findViewById<Button>(R.id.backImageButton)
        dateImage = requireView().findViewById<EditText>(R.id.dateImage)
        timeImage = requireView().findViewById<EditText>(R.id.timeImage)
        saveImageButton = requireView().findViewById<Button>(R.id.saveImageButton)
        chooseImageButton = requireView().findViewById<Button>(R.id.chooseImageButton)
        imageIcon = requireView().findViewById<ImageView>(R.id.imageIcon)
        calendarView = requireView().findViewById<CalendarView>(R.id.calendarView)
        mainCalendarView = requireView().findViewById<CalendarView>(R.id.mainCalendarView)

        //Небольшая заглушка, т.к. календарь не мог появлятся, если изначально был в GONE
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        mainCalendarView.visibility = View.GONE
        calendarView.visibility = View.GONE
        calendarView.layoutParams = params
        mainCalendarView.layoutParams = params
    }

    protected open fun setUpButtons() {
        Log.d("MyTag", "стартуем")
    }

    protected fun defSetup() {
        initDefElements()

        dbHelper = DBHelper(requireContext())
        chosenDate = dbHelper.getChosenDate()
        mainActivity = (activity as MainActivity)
        val apiClient = ApiClient(retrofit)
        userManager = UserManager(apiClient)
        roomManger = RoomManager(apiClient)
        eventManager = EventManager(apiClient)
        taskManager = TaskManager(apiClient)
        imageManager = ImageManager(apiClient)
        setUpButtons()
        setUpDefButtons()
        addParamsToButtons(point0)
        initTimePicker()
        user = dbHelper.getUser()
    }

    private fun setUpDefButtons(){
        addButton.setOnClickListener {
            showDialog()
        }
        addNewPoint.setOnClickListener {
            addNewPoint()
        }
        deletePoint.setOnClickListener {
            deletePoint()
        }
        dataPickerButton.setOnClickListener {
            mainCalendarView.visibility = View.VISIBLE
        }
        backTaskButton.setOnClickListener {
            hideTaskPanel()
            clearTaskPanel()
        }
        backEventButton.setOnClickListener {
            hideEventPanel()
            clearEventPanel()
        }
        dateEvent.setOnClickListener {
            openCalendar()
        }
        dateTask.setOnClickListener {
            openCalendar()
        }
        dateImage.setOnClickListener {
            openCalendar()
        }
        timeEvent.setOnClickListener {
            timePickerDialog.show()
        }
        timeTask.setOnClickListener {
            timePickerDialog.show()
        }
        timeImage.setOnClickListener {
            timePickerDialog.show()
        }
        backImageButton.setOnClickListener{
            clearImagePanel()
            hideImagePanel()
        }
        chooseImageButton.setOnClickListener{
            chooseImage()
            selectedImageUri = null
        }
        saveImageButton.setOnClickListener{
            if(selectedImageUri != null)
                addNewImageIntoScrollView()
        }
        mainCalendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Преобразование выбранной даты в строку
            if (month < 10)
                chosenDate = "$dayOfMonth.0${month + 1}.$year"
            else
                chosenDate = "$dayOfMonth.${month + 1}.$year"

            // Скрытие календаря
            mainCalendarView.visibility = View.GONE

            dataPickerButton.text = chosenDate
            dbHelper.updateChosenDate(chosenDate)
            changeScrollView()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_rooms, container, false)
        return view
    }

    override fun onResume() {
        super.onResume()
    }

    protected fun createEventForAPI(newEvent: Event) {
        eventManager.getAllEvents(object : GetAllEventsCallback {
            override fun onSuccess(events: List<Event>) {
                if (events.isNotEmpty()) {
                    newEvent.idEvent = (events.last().idEvent + 1)
                } else {
                    newEvent.idEvent = 1
                }
                eventManager.createEvent(newEvent, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        //createAllEventsAndTasksAndImages()
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
    private fun getEventsByDateForAPI() {
        events = mutableListOf()
        eventManager.getAllEventsByIdRoom(idRoomDef, object : GetAllEventsCallback {
            override fun onSuccess(tempEvents: List<Event>) {
                for (event in tempEvents) {
                    if (event.date == chosenDate) {
                        events += event
                    }
                }
                events.sortedBy { it.time }
                //createAllEventsAndTasksAndImages()

            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    protected fun updateEventForAPI(previousEvent: Event, updatingEvent: Event) {
        eventManager.updateEvent(previousEvent, updatingEvent, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //changeScrollView()
                createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })

    }

    protected fun deleteEventForAPI(deletingEvent: Event) {
        eventManager.deleteEvent(deletingEvent, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                events.remove(deletingEvent)
                createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }


    protected fun createTaskForAPI(newTask: Task) {
        taskManager.getAllTasks(object : GetAllTaskCallback {
            override fun onSuccess(tasks: List<Task>) {
                if (tasks.isNotEmpty()) {
                    newTask.idTask = (tasks.last().idTask + 1)
                } else {
                    newTask.idTask = 1
                }
                taskManager.createTask(newTask, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
                        //createAllEventsAndTasksAndImages()
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
    protected fun getTasksByDateForAPI() {
        tasks = mutableListOf<Task>()
        taskManager.getAllTasksByIdRoom(idRoomDef, object : GetAllTaskCallback {
            override fun onSuccess(tempTasks: List<Task>) {
                for (task in tempTasks) {
                    if (task.date == chosenDate) {
                        tasks += task
                    }
                }
                tasks.sortedBy { it.time }

                //createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    protected fun updateTaskForAPI(previousTask: Task, updatingTask: Task) {
        taskManager.updateTask(previousTask, updatingTask, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //changeScrollView()
                createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    protected fun deleteTaskForAPI(deletingTask: Task) {
        taskManager.deleteTask(deletingTask, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                tasks.remove(deletingTask)
                createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun createImageForAPI(url: String) {
        val image = this.images.last()
        imageManager.getAllImages(object : GetAllImagesCallback {
            override fun onSuccess(images: List<Image>) {
                image.url = url
                if (images.isNotEmpty()) {
                    image.idImage = (images.last().idImage + 1)
                } else {
                    image.idImage = 1
                }
                imageManager.createImage(image, object : CreateMessageCallback {
                    override fun onSuccess(message: String) {
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
    private fun getImagesByDateForAPI() {
        images = mutableListOf()
        imageManager.getAllImagesByIdRoom(idRoomDef, object : GetAllImagesCallback {
            override fun onSuccess(tempImages: List<Image>) {
                for (image in tempImages) {
                    if (image.date == chosenDate) {
                        images += image
                    }
                }
                events.sortedBy { it.time }
                createAllEventsAndTasksAndImages()

            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    protected fun deleteImageForAPI(deletingImage: Image) {
        imageManager.deleteImage(deletingImage, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                images.remove(deletingImage)

                // Удаление изображения из бакета Timeweb Cloud
                val thread = Thread {
                    try {
                        val minioClient = MinioClient.builder()
                            .endpoint("https://s3.timeweb.cloud")
                            .credentials("CG4IMNYH6V42KN9PNC68", "hTGbzvCw3xmaJZgcW0dPgiDf52BOdFB6b7YsZ7yf")
                            .build()

                        // Извлекаем имя файла из URL изображения
                        val fileName = deletingImage.url.substringAfterLast("/")

                        // Удаление файла из бакета
                        minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                .bucket("9f168657-helper-image-server")
                                .`object`(fileName)
                                .build()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start()

                createAllEventsAndTasksAndImages()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_LONG).show()
            }
        })
    }


    protected open fun rebuildPage() {
        changeScrollView()
    }

    private fun createNewObject(item: Int) {
        saveEventButton.setOnClickListener {
            addNewEventIntoScrollView()
        }
        saveTaskButton.setOnClickListener {
            addNewTaskIntoScrollView()
        }
        when (item) {
            0 -> {
                taskTextView.text = "Создание задачи"
                //Делаем задачу
                clearEventPanel()
                clearTaskPanel()
                countOfPoint = 1
                createTaskPanel.visibility = View.VISIBLE
                addButton.isEnabled = false
            }

            1 -> {
                eventTextView.text = "Создание события"
                //Делаем событие
                clearTaskPanel()
                clearEventPanel()
                createEventPanel.visibility = View.VISIBLE
                addButton.isEnabled = false
            }

            2 -> {
                imageTextView.text = "Создание изображения"
                clearImagePanel()
                createImagePanel.visibility = View.VISIBLE
                addButton.isEnabled = false
            }
        }
    }

    private val PICK_IMAGE_REQUEST = 1

    protected fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageIcon.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToBucket(imageUri: Uri?) {
        val thread = Thread {
            try {
                val minioClient = MinioClient.builder()
                    .endpoint("https://s3.timeweb.cloud")
                    .credentials("CG4IMNYH6V42KN9PNC68", "hTGbzvCw3xmaJZgcW0dPgiDf52BOdFB6b7YsZ7yf")
                    .build()

                // Открываем InputStream для файла
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)

                // Получаем имя файла из Uri
                val fileName = getFileNameFromUri(imageUri)

                // Загружаем файл в бакет TimeWeb Cloud
                try {
                    minioClient.putObject(
                        PutObjectArgs.builder()
                            .bucket("9f168657-helper-image-server")
                            .`object`(fileName) // Используем извлеченное имя файла
                            .stream(inputStream, -1, 10485760)
                            .contentType("image/jpeg").build()
                    )
                } finally {
                    inputStream?.close()
                }

                // Формируем URL для загруженного изображения
                val imageUrl = "https://9f168657-helper-image-server.s3.timeweb.cloud/$fileName"
                createImageForAPI(imageUrl)
                images.last().url = imageUrl

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }


    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "image.jpg" // Имя файла по умолчанию

        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    protected fun addNewImageIntoScrollView() {
        val image = Image(
            (images.lastIndex + 1).toLong(),
            idRoomDef,
            stringToDate(dateImage.text.toString().trim()),
            stringToTime(timeImage.text.toString().trim()),
            ""
        )

        // Проверяем размер изображения
        val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
        val fileSizeInBytes = inputStream?.available()?.toLong() ?: 0
        val fileSizeInMB = fileSizeInBytes / (1024 * 1024)

        inputStream?.close()

        if (fileSizeInMB > 10) {
            createError("Ошибка: размер изображения превышает 10 МБ")
            return
        }

        // Запуск потока для загрузки изображения в Timeweb Cloud
        uploadImageToBucket(selectedImageUri)

        createError("Создано на ${image.date}")
        if (chosenDate == image.date) {
            images += image
            createAllEventsAndTasksAndImages(false)
        }

        hideImagePanel()
        clearImagePanel()
    }



    private fun createNewImage(i : Int,isFromMySQL : Boolean){
        if (mainLayout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            mainLayout.removeView(mainLayout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val image = images[i]
        val imageView = ImageView(requireContext())
        imageView.minimumWidth = 200
        imageView.minimumHeight = 200
        imageView.maxHeight = 850
        imageView.adjustViewBounds = true

        if(isFromMySQL) {
            Picasso.get()
                .load(image.url)
                .into(imageView)
        }
        else {
            imageView.setImageURI(selectedImageUri)
        }

        imageView.id = i + 55536
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)

        if (mainLayout.childCount == 0 || (mainLayout.childCount == 1 && mainLayout.getChildAt(
                0
            ) == deleteButton)
        ) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
        } else if (mainLayout.getChildAt(mainLayout.childCount - 1) != deleteButton) {
            params.addRule(
                RelativeLayout.BELOW,
                mainLayout.getChildAt(mainLayout.childCount - 1).id
            )
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                mainLayout.getChildAt(mainLayout.childCount - 2).id
            )
        }

        imageView.setLayoutParams(params)
        mainLayout.addView(imageView)
        setupLongClickListeners(imageView, i)
    }


    protected fun addNewEventIntoScrollView() {
        if (eventEvent.text.toString().trim().isEmpty()) {
            createError("Ошибка! Нет описания!")
            return
        }

        //Сохраняем в БД
        val event = Event(
            0,
            idRoomDef,
            stringToDate(dateEvent.text.toString()),
            stringToTime(timeEvent.text.toString()),
            placeEvent.text.toString(),
            eventEvent.text.toString()
        )

        createError("Созданно на " + event.date)

        if (chosenDate == event.date) {
            events += event
            createAllEventsAndTasksAndImages()
        }

        hideEventPanel()
        clearEventPanel()
        createEventForAPI(event)
    }

    protected fun addNewTaskIntoScrollView() {
        //Добавляем пункты
        var points: List<String> = ArrayList()
        var checkBoxes: List<Boolean> = ArrayList()
        if(point0.text.toString().trim().isNotEmpty() && point0.text.toString().trim()!=""){
            points += point0.text.toString().trim()
            checkBoxes += false
        }

        var j = 1
        while (countOfPoint > j) {
            if (pointsPlace.findViewById<EditText>(j + TASK_ID).text.trim().toString()
                    .isNotEmpty()
            ) {
                points += pointsPlace.findViewById<EditText>(j + TASK_ID).text.toString()
                    .trim()
                checkBoxes += false
            }
            j += 1
        }

        if (timeTask.text.toString().trim().isEmpty()) {
            timeTask.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
        }

        if(points.isNotEmpty()) {
            //Сохраняем в БД
            val newTask = Task(
                0,
                idRoomDef,
                stringToDate(dateTask.text.toString().trim()),
                stringToTime(timeTask.text.toString().trim()),
                nameTask.text.toString().trim(),
                points.joinToString("|"),
                checkBoxes.map { it.toString() }.joinToString("|")
            )

            if (chosenDate == newTask.date) {
                tasks += newTask
                createAllEventsAndTasksAndImages()
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
    protected fun createNewTask(task: Task) {
        if (mainLayout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            mainLayout.removeView(mainLayout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val layout = createRelativeLayout(tasks.indexOf(task))
        //val nameTextView: TextView

        /*if (task.name.isEmpty())
            nameTextView = createText("Без названия", true)
        else
            nameTextView = createText(task.name, true)*/
        //layout.addView(nameTextView)
        //nameTextView.id = 666
        //nameTextView.textSize = textSize + 1
        //nameTextView.gravity = Gravity.CENTER

        val points: List<String> = task.points.splitToSequence("|").toList()
        val checkBoxes: List<Boolean> = task.checkBoxes.split("|").map { it.toBoolean() }

        if (points.count() > points.count()) {
            repairTask(task, points)
        }

        var j = 0
        while (points.count() > j) {
            val textView = createText(points[j])
            addParamsToNewPoint(textView, layout, j, checkBoxes[j])
            j += 1
        }

        layout.setBackgroundResource(R.drawable.border_task)
        mainLayout.addView(layout)
        setupLongClickListeners(layout, tasks.indexOf(task))
    }

    private fun repairTask(task: Task, points:List<String>) {
        val tempPoints: MutableList<String> = ArrayList<String>().toMutableList()
        var i = 0
        while (i < points.count() - 2) {
            tempPoints += points[i]
            i += 1
        }
        tempPoints += (points[points.count() - 2] + points[points.count() - 1])
        val newTask = task
        newTask.points = tempPoints.joinToString("|")
        updateTaskForAPI(task,newTask)
    }

    protected fun createNewEvent(i: Int) {
        if (mainLayout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            mainLayout.removeView(mainLayout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val textView: TextView

        if (events[i].place.isNotEmpty() && events[i].time.length < 7) {
            textView = createText(
                events[i].time + System.lineSeparator() +
                        events[i].place + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else if (events[i].time.length < 7) {
            textView = createText(
                events[i].time + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else if (events[i].place.isNotEmpty() && events[i].event.isNotEmpty()) {
            textView = createText(
                events[i].place + System.lineSeparator() +
                        events[i].event, false, true
            )
        } else {
            textView = createText(events[i].event, false, true)
        }

        textView.setBackgroundResource(R.drawable.border_event)
        textView.id = i + ENENT_ID
        mainLayout.addView(textView)
        setupLongClickListeners(textView, i)
    }

    protected fun addNewPoint(text: String = "") {
        val editText = EditText(context)
        //Подвинуть пункт
        addParamsToEditText(editText,text)
        //Подвинуть кнопки
        addParamsToButtons(editText)
        countOfPoint += 1
    }

    //TODO: добавить возможность менять цветa в настройках
    @SuppressLint("ResourceAsColor")
    protected fun createText(
        text: String,
        isNameText: Boolean = false,
        isNeedBelow: Boolean = false
    ): TextView {
        val textView = TextView(context)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)

        if (isNameText) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            params.setMargins(1, 1, 1, 1)
        }

        if (isNeedBelow) {
            textView.id = 1488632223 + events.size
            if (mainLayout.childCount == 0 || (mainLayout.childCount == 1 && mainLayout.getChildAt(
                    0
                ) == deleteButton)
            ) {
                params.addRule(RelativeLayout.ALIGN_PARENT_START)
            } else if (mainLayout.getChildAt(mainLayout.childCount - 1) != deleteButton) {
                params.addRule(
                    RelativeLayout.BELOW,
                    mainLayout.getChildAt(mainLayout.childCount - 1).id
                )
            } else {
                params.addRule(
                    RelativeLayout.BELOW,
                    mainLayout.getChildAt(mainLayout.childCount - 2).id
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
    protected fun createButton(text: String): Button {
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

        if (mainLayout.childCount == 0 || (mainLayout.childCount == 1 && mainLayout.getChildAt(
                0
            ) == deleteButton)
        ) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
        } else if (mainLayout.getChildAt(mainLayout.childCount - 1) != deleteButton) {
            params.addRule(
                RelativeLayout.BELOW,
                mainLayout.getChildAt(mainLayout.childCount - 1).id
            )
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                mainLayout.getChildAt(mainLayout.childCount - 2).id
            )
        }

        layout.setLayoutParams(params)
        layout.setPadding(0, 0, 0, 10)
        return layout
    }

    @SuppressLint("ResourceAsColor")
    protected fun addParamsToEditText(editText: EditText, text: String = "") {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        //Установить новый пункт
        params.setMargins(30, 30, 30, 30)
        editText.setLayoutParams(params)
        editText.setBackgroundResource(R.color.edit_text)
        editText.hint = "Задача " + (countOfPoint + 1).toString()
        editText.setText(text)
        editText.id = countOfPoint + TASK_ID
        editText.setPadding(10, 10, 10, 40)
        pointsPlace.addView(editText)
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))

        //Подвинуть новый пункт
        if (countOfPoint == 1) {
            params.addRule(RelativeLayout.BELOW, point0.id)
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                createTaskPanel.findViewById<EditText>(countOfPoint + TASK_ID - 1).id
            )
        }
        editText.setLayoutParams(params)
    }

    @SuppressLint("ResourceType")
    protected fun addParamsToNewPoint(
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
            checkBoxParams.addRule(RelativeLayout.ALIGN_PARENT_START)
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

    protected fun addParamsToButtons(editText: EditText) {
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
        saveTaskButton.setLayoutParams(btn1Params)

        btn2Params.addRule(RelativeLayout.ALIGN_END, editText.id)
        btn2Params.addRule(RelativeLayout.BELOW, editText.id)
        addNewPoint.setLayoutParams(btn2Params)

        btn3Params.addRule(RelativeLayout.BELOW, editText.id)
        btn3Params.addRule(RelativeLayout.LEFT_OF, addNewPoint.id)
        deletePoint.setLayoutParams(btn3Params)
    }

    @SuppressLint("SetTextI18n")
    protected fun changeBackgroundOfPoint(textView: TextView, checkBox: CheckBox, i: Int, j: Int) {
        val newCheckBoxes = tasks[i].checkBoxes.split("|").map { it.toBoolean() }.toMutableList()
        val checkboxStateChanged = newCheckBoxes[j] != checkBox.isChecked

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
        if (checkboxStateChanged) {
            val newTask = tasks[i]
            newTask.checkBoxes = newCheckBoxes.map { it.toString() }.joinToString("|")
            updateTaskForAPI(tasks[i],newTask)
            tasks[i] = newTask
        }
    }

    protected fun clearImagePanel(){
        dateImage.setText("")
        timeImage.setText("")
        imageIcon.setImageURI(null)
    }

    protected fun clearEventPanel() {
        dateEvent.setText("")
        timeEvent.setText("")
        placeEvent.setText("")
        eventEvent.setText("")
    }

    protected fun clearTaskPanel() {
        dateTask.setText("")
        timeTask.setText("")
        nameTask.setText("")
        point0.setText("")
        addParamsToButtons(point0)

        while (countOfPoint > 1) {
            pointsPlace.removeView(
                createTaskPanel.findViewById<EditText>(
                    countOfPoint + TASK_ID - 1
                )
            )
            countOfPoint -= 1
        }
    }

    protected fun deletePoint() {
        if (countOfPoint > 2) {
            pointsPlace.removeView(pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID - 1))
            countOfPoint -= 1
            addParamsToButtons(pointsPlace.findViewById(countOfPoint + TASK_ID - 1))
        } else if (countOfPoint > 1) {
            pointsPlace.removeView(pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID - 1))
            countOfPoint -= 1
            addParamsToButtons(point0)
        } else {
            createError("Ошибка! Нельзя удалить этот пункт!")
        }
    }

    protected fun createError(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    protected fun hideTaskPanel() {
        addButton.isEnabled = true
        createTaskPanel.visibility = View.GONE
    }

    protected fun hideImagePanel(){
        addButton.isEnabled = true
        createImagePanel.visibility = View.GONE
    }

    protected fun hideEventPanel() {
        addButton.isEnabled = true
        createEventPanel.visibility = View.GONE
    }

    protected fun stringToDate(string: String): String {
        val newString: String

        if (string.length == 8)
            newString = string.replace(Regex("(\\d{2})$"), "20$1")
        else if (string.isEmpty())
            newString = chosenDate
        else
            newString = string

        return newString
    }

    protected fun stringToTime(string: String): String {
        val newString: String
        if (string.isEmpty())
            newString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        else
            newString = string

        return newString
    }
    protected open fun setupLongClickListeners(view: View, id: Int) {
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

            if (mainLayout.indexOfChild(view) == 0) {
                params.addRule(RelativeLayout.BELOW, view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            } else {
                params.addRule(RelativeLayout.ABOVE, view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            }
            deleteButton.layoutParams = params
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

                    is ImageView -> {
                        deleteImageForAPI(images[id])
                    }
                }
            }
            if (mainLayout.indexOfChild(view) == 0) {
                paramsToEdit.addRule(RelativeLayout.BELOW, view.id)
                paramsToEdit.addRule(RelativeLayout.ALIGN_RIGHT, view.id)
            } else {
                paramsToEdit.addRule(RelativeLayout.ABOVE, view.id)
                paramsToEdit.addRule(RelativeLayout.ALIGN_RIGHT, view.id)
            }

            if (view !is ImageView) {
                editButton.text = "Редактировать"
                editButton.layoutParams = paramsToEdit
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
            }
            else{

                editButton.text = "Скачать"
                editButton.layoutParams = paramsToEdit
                editButton.visibility = View.VISIBLE

                editButton.setOnClickListener {
                    deleteButton.visibility = View.GONE
                    editButton.visibility = View.GONE

                    downloadImage(images[id])
                    Toast.makeText(requireContext(), "Изображение сохранено в галерею", Toast.LENGTH_LONG).show()
                }
            }

            true
        }
        setListeners(view)
    }

    private fun downloadImage(image: Image) {
        val thread = Thread {
            try {
                // Получаем изображение по URL
                val url = URL(image.url)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Сохраняем изображение в галерею
                saveImageToGallery(bitmap,image)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    private fun saveImageToGallery(bitmap: Bitmap,image:Image) {
        val tempName = image.url.substringAfterLast("/")

        val filename = "${tempName.substringBeforeLast(".")}.jpg"
        val fos: OutputStream

        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(imageUri!!)!!

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        // Уведомляем медиа-сканер о новом изображении
        MediaScannerConnection.scanFile(
            requireContext(),
            arrayOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + filename),
            null,
            null
        )

    }

    protected open fun setListeners(view: View){}
    @SuppressLint("ClickableViewAccessibility")
    protected open fun setTouchListenerForButtons(view: View,) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deleteButton.visibility = View.GONE
                editButton.visibility = View.GONE
                calendarView.visibility = View.GONE
                mainCalendarView.visibility = View.GONE
            }
            false
        }
    }
    protected fun editEvent(event: Event) {
        eventTextView.text = "Редактирование события"
        createEventPanel.visibility = View.VISIBLE
        dateEvent.setText(event.date)
        if(event.time.length < 7){
            timeEvent.setText(event.time)
        }
        else{
            timeEvent.setText(event.time.substring(0, event.time.length - 2))
        }
        placeEvent.setText(event.place)
        eventEvent.setText(event.event)
        addButton.isEnabled = false
        saveEventButton.setOnClickListener {

            if (timeEvent.text.toString().isEmpty()) {
                timeEvent.setText(event.time)
            }
            val newEvent = Event(
                0,
                idRoomDef,
                stringToDate(dateEvent.text.toString()),
                stringToTime(timeEvent.text.toString()),
                placeEvent.text.toString(),
                eventEvent.text.toString()
            )
            if(newEvent.date != event.date || newEvent.time !=event.time || newEvent.place != event.place || newEvent.event != event.event){
                updateEventForAPI(event,newEvent)
                events[events.indexOf(event)] = newEvent
            }

            clearEventPanel()
            hideEventPanel()
        }
    }

    private fun editTask(task: Task) {
        taskTextView.text = "Редактирование задачи"
        createTaskPanel.visibility = View.VISIBLE
        dateTask.setText(task.date)
        if(task.time.length < 7){
            timeEvent.setText(task.time)
        }
        else{
            timeEvent.setText(task.time.substring(0, task.time.length - 2))
        }
        nameTask.setText(task.name)
        val previousPoints: List<String> = task.points.splitToSequence("|").toMutableList()
        point0.setText(previousPoints[0])
        addParamsToButtons(point0)

        var i = 1
        while (previousPoints.count() > i) {
            addNewPoint(previousPoints[i])
            i+=1
        }

        saveTaskButton.setOnClickListener {
            if(previousPoints.isNotEmpty()) {
                var points: List<String> = ArrayList()
                var checkBoxes: List<Boolean> = ArrayList()
                if(point0.text.trim().toString().isNotEmpty()){
                    points += point0.text.toString()
                    checkBoxes += false
                }

                var j = 1
                while (countOfPoint > j) {
                    if (pointsPlace.findViewById<EditText>(j + TASK_ID).text.trim().toString()
                            .isNotEmpty()
                    ) {
                        points += pointsPlace.findViewById<EditText>(j + TASK_ID).text.toString()
                            .trim()
                        checkBoxes += false
                    }
                    j += 1
                }

                if(timeTask.text.toString().isEmpty()){
                    timeTask.setText(task.time)}

                val newTask = Task(
                    0,
                    idRoomDef,
                    stringToDate(dateTask.text.toString()),
                    stringToTime(timeTask.text.toString()),
                    nameTask.text.toString(),
                    points.joinToString("|"),
                    checkBoxes.map { it.toString() }.joinToString("|")
                )
                if(newTask.date != task.date || newTask.time !=task.time || newTask.name != task.name || newTask.points != task.points){
                    tasks[tasks.indexOf(task)] = newTask
                    updateTaskForAPI(task,newTask)
                }
            }else{
                createError("Ошибка! Задача была пуста")
            }
            clearTaskPanel()
            hideTaskPanel()
        }
    }

    protected fun createAllEventsAndTasksAndImages(isFromMysql:Boolean = true) {
        mainLayout.removeAllViews()

        val newList = (events + tasks + images).sortedBy { it.time }
        for (item in newList) {
            if(images.isNotEmpty() && item == images.last() && !isFromMysql){
                createNewImage(images.indexOf(item),isFromMysql)
            }
            else {
                when (item) {
                    is Event -> createNewEvent(events.indexOf(item))
                    is Task -> createNewTask(item)
                    is Image -> createNewImage(images.indexOf(item), true)
                }
            }
        }

        mainLayout.addView(deleteButton)
        mainLayout.addView(editButton)
        checkToNothingToDo()
    }

    private fun checkToNothingToDo() {
        if (mainLayout.childCount == 2) {
            val textView = createText("На этот день ничего не запланировано")
            textView.setTextColor(Color.GRAY)
            textView.id = TEXT_VIEW_NOTHING_TO_DO_ID
            mainLayout.addView(textView)
        }
    }

    protected fun changeScrollView() {
        getEventsByDateForAPI()
        getTasksByDateForAPI()
        getImagesByDateForAPI()
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

            timeEvent.setText(time)
            timeTask.setText(time)
            timeImage.setText(time)
        }

        val cal: Calendar = Calendar.getInstance()
        val hour: Int = cal.get(Calendar.HOUR_OF_DAY)
        val minute: Int = cal.get(Calendar.MINUTE)

        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        timePickerDialog =
            TimePickerDialog(requireContext(), style, timeSetListener, hour, minute, true)
    }


    private fun openCalendar(){
        calendarView.visibility = View.VISIBLE
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Преобразование выбранной даты в строку
            var date =""
            if (month < 10)
                date = "$dayOfMonth.0${month + 1}.$year"
            else
                date = "$dayOfMonth.${month + 1}.$year"
            calendarView.visibility = View.GONE

            dateEvent.setText(date)
            dateTask.setText(date)
            dateImage.setText(date)
        }
    }


    private fun showDialog() {
        val langArray: Array<String> = arrayOf("Задача", "Мероприятие", "Изображение")
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
            createNewObject(selectedEvent)
        }

        builder.setNegativeButton(
            "Отмена"
        ) { dialogInterface, _ -> // закрыть диалог
            dialogInterface.dismiss()
        }

        builder.show()
    }
}