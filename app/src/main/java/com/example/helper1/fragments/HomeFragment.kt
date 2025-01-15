package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
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
                } else if(passwordUser.text.toString().trim().length < 7){
                    createError("Минимальная длина пароля 8 символов")
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
        setKey()
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
        else{
            mainActivity.checkIsHaveLog()
        }
    }

    private fun setUpElements(){
        showUsersButton.visibility = View.GONE
        roomNameTextView.visibility = View.GONE
        showRoomPanelButton.visibility = View.GONE
        requireView().findViewById<TextView>(R.id.showRoomPanelTextView).visibility = View.GONE

        val scrView = requireView().findViewById<ScrollView>(R.id.scrView)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_START)
        params.addRule(RelativeLayout.ABOVE, R.id.sortButton)
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

        val paramsSortButton = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        paramsSortButton.addRule(RelativeLayout.ALIGN_END, addButton.id)
        paramsSortButton.addRule(RelativeLayout.ABOVE, addButton.id)
        sortButton.layoutParams = paramsSortButton

    }

    private fun updateLocalUser(){
        userManager.getUser(user!!.login, object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                gotUser.password = unHashPassword(gotUser.password)
                val nowRooms: List<Int> = user!!.availableRooms
                    .split("|")
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }

                val roomsInDB: List<Int> = gotUser.availableRooms
                    .split("|")
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }

                val kickedRoomsId: List<Int> = nowRooms.filter { it !in roomsInDB }

                if(kickedRoomsId.isNotEmpty()){
                    roomManger.getAllRooms(object : GetAllRoomsCallback {
                        override fun onSuccess(rooms: List<Room>) {
                            // Создаем диалоговое окно
                            val dialogBuilder = AlertDialog.Builder(requireContext())

                            dialogBuilder.setTitle("Уведомляем")
                            var text = ""
                            val kickedRoomList = rooms.filter { it.idRoom in kickedRoomsId }
                            if(kickedRoomsId.count() == 1){
                                dbHelper.updateRoomId(-1)
                                text = "Вас выгнали из комнаты '${kickedRoomList[0].name}'"
                            }
                            else{
                                var kickedName = ""
                                for (room in kickedRoomList){
                                    if(room == kickedRoomList.last()){
                                        kickedName += "'${room.name}'"
                                    }
                                    else{
                                        kickedName += "'${room.name}'" + ", "
                                    }
                                }
                                text = "Вас выгнали из комнат: $kickedName"
                            }
                            dialogBuilder.setMessage(text)

                            dialogBuilder.setNeutralButton("Ок") { dialog, _ ->
                                dialog.dismiss()
                            }

                            val alertDialog = dialogBuilder.create()
                            alertDialog.show()
                        }

                        override fun onFailure(message: String) {
                            createError(message)
                        }
                    })
                }
                user!!.password = gotUser.password
                user!!.ownRoom = gotUser.ownRoom
                user!!.availableRooms = gotUser.availableRooms
                dbHelper.updateUser(user!!)
                createUserPanel.visibility = View.GONE
            }

            override fun onFailure(isExist: Boolean) {

                createError("Ошибка! Пользователь не найден")
            }
        })
    }



    private fun loginUserForAPI(loggingUser : User){
        userManager.getUser(loginUser.text.toString().trim(),object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                if (loggingUser.password == unHashPassword(gotUser.password)) {
                    loggingUser.availableRooms = gotUser.availableRooms
                    loggingUser.ownRoom = gotUser.ownRoom
                    idRoomDef = gotUser.ownRoom
                    mainActivity.startActivity()
                    dbHelper.createUser(loggingUser)
                    setUpElements()
                    rebuildPage()
                    createUserPanel.visibility = View.GONE
                    addButton.visibility = View.VISIBLE
                    dataPickerButton.visibility = View.VISIBLE
                }else{

                    createError("Неверный пароль!")
                }
            }


            override fun onFailure(isExist: Boolean) {
                createError("Ошибка! Пользователь не найден")
            }
        })

        Log.d("MyTag",loggingUser.toString())
    }

    private fun createUserForAPI() {
        val newRoom = Room(
            0,
            loginUser.text.toString().trim(),
            hashPassword(passwordUser.text.toString().trim()),
            true,
            loginUser.text.toString().trim(),
            "",
            ""
        )
        userManager.getUser(loginUser.text.toString().trim(), object : GetUserCallback {
            override fun onSuccess(user: User) {
                createError("Ошибка! Такой логин уже существует!")
                //Проверка на уникальность логина
            }

            override fun onFailure(isExist: Boolean) {
                //Создаем личную комнату под юзера
                createRoomForAPI(newRoom)
            }
        })
    }

    private fun createUser(idRoom: Int) {
        val newUser = User(
            loginUser.text.toString().trim(),
            hashPassword(passwordUser.text.toString().trim()),
            idRoom,
            ""
        )
        userManager.createUser(newUser, object : CreateUserCallback {
            override fun onSuccess(message: String) {
                createError(message)
            }

            override fun onUserCreated(user: User) {
                user.password = unHashPassword(user.password)
                dbHelper.createUser(user)
                mainActivity.startActivity()
                setUpElements()
                createUserPanel.visibility = View.GONE
                addButton.visibility = View.VISIBLE
                dataPickerButton.visibility = View.VISIBLE
            }

            override fun onFailure(message: String) {
                createError(message)
            }
        })
    }

    private fun createRoomForAPI(newRoom: Room) {
        roomManger.getAllRooms(object : GetAllRoomsCallback {
            override fun onSuccess(rooms: List<Room>) {
                var idRoom: Int = 0

                if (rooms.isNotEmpty()){
                    idRoom = (rooms.last().idRoom + 1)
                }

                newRoom.idRoom = idRoom
                roomManger.createRoom(newRoom, object : CreateRoomCallback {
                    override fun onSuccess(message: String) {}

                    override fun onFailure(message: String) {}

                    override fun onRoomCreated(idRoom: Int) {
                        idRoomDef = idRoom
                        createUser(idRoomDef)
                    }
                })
            }

            override fun onFailure(message: String) {
                createError(message)
            }
        })
    }

    override fun setListeners(view: View){
        if(this.view != null) {
            setTouchListenerForButtons(view)
            setTouchListenerForButtons(mainLayout)
            setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
        }
    }
}