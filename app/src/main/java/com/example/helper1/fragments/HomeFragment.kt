package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.util.Log
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
            updateLocalUser()
            mainActivity.startActivity()
        }

        loginUserButton.setOnClickListener{
            val loggingUser = User(
                loginUser.text.toString().trim(),
                passwordUser.text.toString().trim(),
                0,
                ""
            )
            if(loggingUser.login.isNotEmpty() && loggingUser.password.isNotEmpty()) {
                loginUserForAPI(loggingUser)
            }else{
                createError("Ошибка! Заполните данные")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        defSetup()
        chosenDate = dbHelper.getChosenDate()
        user = dbHelper.getUser()
        setUpElements()
        addParamsToButtons(point0)
        dataPickerButton.text = chosenDate
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
        setTouchListenerForButtons(requireView().findViewById(R.id.scrView))

        if (user != null) {
            idRoomDef = user!!.ownRoom
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

        val paramsAddButton = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsAddButton.setMargins(10,10,10,10)
        paramsAddButton.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        paramsAddButton.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        addButton.layoutParams = paramsAddButton

    }

    private fun updateLocalUser(){
        userManager.getUser(user!!.login, object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                user!!.password = gotUser.password
                user!!.ownRoom = gotUser.ownRoom
                user!!.availableRooms = gotUser.availableRooms
                dbHelper.updateUser(user!!)
                createUserPanel.visibility = View.GONE
            }

            override fun onFailure(isExist: Boolean) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка! Пользователь не найден",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loginUserForAPI(loggingUser : User){
        userManager.getUser(loginUser.text.toString().trim(),object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                if (loggingUser.password == gotUser.password) {
                    loggingUser.availableRooms = gotUser.availableRooms
                    loggingUser.ownRoom = gotUser.ownRoom
                    idRoomDef = gotUser.ownRoom
                    rebuildPage()
                    mainActivity.startActivity()
                    dbHelper.createUser(loggingUser)
                    setUpElements()
                    createUserPanel.visibility = View.GONE
                    addButton.visibility = View.VISIBLE
                    dataPickerButton.visibility = View.VISIBLE
                }else{
                    Toast.makeText(requireContext(), "Неверный пароль!", Toast.LENGTH_LONG)
                        .show()
                }
            }


            override fun onFailure(isExist: Boolean) {
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
                setUpElements()
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

    override fun setListeners(view: View){
        setTouchListenerForButtons(view)
        setTouchListenerForButtons(mainLayout)
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
    }
}