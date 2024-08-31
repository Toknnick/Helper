package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.helper1.R
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.CreateUserCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetUserCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.User

@SuppressLint("SetTextI18n","ClickableViewAccessibility")
@Suppress("DEPRECATION", "NAME_SHADOWING")
class HomeFragment : ParentFragment(){
    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment

    override fun setUpButtons(){
        //requireContext().deleteDatabase(dbHelper.databaseName)
        user = dbHelper.getUser()
        if(user == null){
            addButton.visibility = View.GONE
            dataPickerButton.visibility = View.GONE
            createUserPanel.visibility = View.VISIBLE
            saveUserButton.setOnClickListener{
                if(passwordUser.text.toString().trim().isEmpty()){
                    createError("Пароль не может быть пустым")
                } else if(passwordUser.text.toString().trim().length < 5){
                    createError("Минимальная длина пароля 6 символов")
                } else if(loginUser.text.toString().trim().isEmpty()){
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
            datePickerDialog.show()
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
            datePickerDialogForObject.show()
        }
        dateTask.setOnClickListener {
            datePickerDialogForObject.show()
        }
        timeEvent.setOnClickListener {
            timePickerDialog.show()
        }
        timeTask.setOnClickListener {
            timePickerDialog.show()
        }

        loginUserButton.setOnClickListener{
            val loggingUser = User(
                loginUser.text.toString().trim(),
                passwordUser.text.toString().trim(),
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
        defSetup()
        setUpElements()
        chosenDate = dbHelper.getChosenDate()
        user = dbHelper.getUser()
        addParamsToButtons(point0)
        dataPickerButton.text = chosenDate
        deleteButton = createButton("Удалить")
        editButton = createButton("Редактировать")
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
        setTouchListenerForButtons(requireView().findViewById(R.id.scrView))

        dataPickerButton.text = chosenDate
        if (user != null) {
            rebuildPage()
        }
    }

    private fun setUpElements(){
        roomNameTextView.visibility = View.GONE
        showRoomPanelButton.visibility = View.GONE
        requireView().findViewById<TextView>(R.id.showRoomPanelTextView).visibility = View.GONE
        val scrView = requireView().findViewById<ScrollView>(R.id.scrView)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_START)
        params.addRule(RelativeLayout.ABOVE, R.id.addButton)
        scrView.layoutParams = params

        val paramsDataPickerButton = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsDataPickerButton.setMargins(10,10,10,10)
        paramsDataPickerButton.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        paramsDataPickerButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        dataPickerButton.layoutParams = paramsDataPickerButton

    }

    private fun loginUserForAPI(loggingUser : User, isUpdateUserData : Boolean){
        userManager.getUser(loginUser.text.toString().trim(),object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                if(!isUpdateUserData) {
                    if (loggingUser.password == gotUser.password) {
                        dbHelper.createUser(loggingUser)
                        createUserPanel.visibility = View.GONE
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
            loginUser.text.toString().trim(),
            passwordUser.text.toString().trim(),
            true
        )
        userManager.getUser(loginUser.text.toString().trim(), object : GetUserCallback {
            override fun onSuccess(user: User) {
                //Проверка на уникальность логина
                Toast.makeText(requireContext(),"Ошибка! Такой логин уже существует!", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(isExist: Boolean) {
                //Создаем личную комнату под юзера
                createRoomForAPI(newRoom)
            }
        })
    }

    private fun createUser(idRoom: Long) {
        val newUser = User(
            loginUser.text.toString().trim(),
            passwordUser.text.toString().trim(),
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
                createUserPanel.visibility = View.GONE
                addButton.visibility = View.VISIBLE
                dataPickerButton.visibility = View.VISIBLE
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun createRoomForAPI(newRoom: Room) {
        roomManger.getAllRooms(object : GetAllRoomsCallback {
            override fun onSuccess(rooms: List<Room>) {
                val idRoom = (rooms.count() + 1).toLong()
                newRoom.idRoom = idRoom
                newRoom.single = true
                roomManger.createRoom(newRoom, object : CreateRoomCallback {
                    override fun onSuccess(message: String) {}

                    override fun onFailure(message: String) {}

                    override fun onRoomCreated(idRoom: Long) {
                        idRoomDef = idRoom
                        createUser(idRoomDef)
                    }
                })
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun setupLongClickListeners(view: View, id: Int) {
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

            if (mainLayout.indexOfChild(view) == 0) {
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

        setTouchListenerForButtons(view)
        setTouchListenerForButtons(mainLayout)
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun setTouchListenerForButtons(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deleteButton.visibility = View.GONE
                editButton.visibility = View.GONE
            }
            false
        }
    }

}