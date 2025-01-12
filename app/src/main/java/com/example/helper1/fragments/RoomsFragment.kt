package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.helper1.R
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.GetUserCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.User

@Suppress("NAME_SHADOWING", "DEPRECATION")
class RoomsFragment : ParentFragment() {
    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment
    private lateinit var nowRoom: Room
    override fun setUpButtons() {
        settingsRoomButton.setOnClickListener{
            createRoomPanel.visibility = View.VISIBLE
            editRoom()
        }
        showUsersButton.setOnClickListener{
            showUsersPanel()
        }
        showRoomPanelButton.setOnClickListener {
            showRoomPanel()
        }
        createRoomButton.setOnClickListener{
            createRoomPanel.visibility = View.VISIBLE
            createRoomButton.isEnabled = false
            addRoomButton.isEnabled = false
        }
        addRoomButton.setOnClickListener{
            addRoomPanel.visibility = View.VISIBLE
            createRoomButton.isEnabled = false
            addRoomButton.isEnabled = false
        }
        saveRoomButton.setOnClickListener {
                createRoom()
                createRoomButton.isEnabled = true
                addRoomButton.isEnabled = true
        }
        backCreateRoomButton.setOnClickListener {
            hideRoom()
        }
        getRoomButton.setOnClickListener{
            addRoom()
            createRoomButton.isEnabled = true
            addRoomButton.isEnabled = true
        }
        backAddRoomButton.setOnClickListener{
            addRoomPanel.visibility = View.GONE
            createRoomButton.isEnabled = true
            addRoomButton.isEnabled = true
            addIdRoom.setText("")
            addPasswordRoom.setText("")
        }
    }


    override fun onResume() {
        super.onResume()
        defSetup()
        chosenDate = dbHelper.getChosenDate()
        user = dbHelper.getUser()
        dataPickerButton.text = chosenDate
        rebuildRoomPanel()
        rebuildPage()
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
        setTouchListenerForButtons(requireView().findViewById(R.id.scrView))
    }

    private fun createRoomForAPI(newRoom: Room) {
        roomManger.getAllRooms(object : GetAllRoomsCallback {
            override fun onSuccess(rooms: List<Room>) {
                val idRoom = (rooms.last().idRoom + 1)
                newRoom.idRoom = idRoom
                roomManger.createRoom(newRoom, object : CreateRoomCallback {
                    override fun onSuccess(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        if(user!!.availableRooms != "") {
                            user!!.availableRooms += "|${newRoom.idRoom}"
                        }else{
                            user!!.availableRooms += "${newRoom.idRoom}"
                        }
                        rebuildRoomPanel()
                        updateUserForAPI(user!!)
                    }

                    override fun onFailure(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }

                    override fun onRoomCreated(idRoom: Long) {
                        idRoomDef = idRoom
                    }
                })
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getRoomFromAPI(gettingRoom: Room, isForText: Boolean) {
        roomManger.getRoom(gettingRoom.idRoom, object : GetRoomCallback {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(gotRoom: Room) {
                gotRoom.password = unHashPassword(gotRoom.password)
                if(!gotRoom.single) {
                    if (isForText) {
                        nowRoom = gotRoom

                        if(nowRoom.owner == user!!.login)
                            settingsRoomButton.visibility = View.VISIBLE

                        roomNameTextView.text = gotRoom.name
                        sortButton.visibility = View.VISIBLE
                        showUsersButton.visibility = View.VISIBLE
                        addButton.visibility = View.VISIBLE
                        dataPickerButton.visibility = View.VISIBLE
                    } else {
                        if (gettingRoom.password == gotRoom.password) {
                            if (user!!.availableRooms != "") {
                                user!!.availableRooms += "|${gettingRoom.idRoom}"
                            } else {
                                user!!.availableRooms += "${gettingRoom.idRoom}"
                            }
                            if(!gotRoom.bannedUsers.contains(user!!.login) ){
                                gettingRoom.name = gotRoom.name
                                gettingRoom.owner = gotRoom.owner
                                gettingRoom.bannedUsers = gotRoom.bannedUsers
                                if (gotRoom.users != ""){
                                    gettingRoom.users = gotRoom.users + "|" + user!!.login
                                }
                                else{
                                    gettingRoom.users = user!!.login
                                }
                                updateRoomForAPI(gettingRoom)

                                idRoomDef = gettingRoom.idRoom
                                nowRoom = gotRoom
                                rebuildRoomPanel()
                                updateUserForAPI(user!!)
                            }
                            else{
                                createError("Дверь в эту комнату вам закрыта")
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Ошибка! Данные не верны!",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }else{
                    Toast.makeText(requireContext(), "Ошибка! комната не найдена!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(message: String) {
                if (!isForText) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun updateRoomForAPI(newRoom : Room){
        newRoom.password = hashPassword(newRoom.password)
        nowRoom.password = unHashPassword(newRoom.password)
        roomManger.updateRoom(newRoom, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserForAPI(newUser: User) {
        newUser.password = hashPassword(newUser.password)
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                user!!.password = unHashPassword(newUser.password)
                dbHelper.updateUser(user!!)
                dbHelper.updateRoomId(idRoomDef)
                rebuildPage()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, true)
    }

    override fun rebuildPage(){
        hideRoomPanel()
        idRoomDef = dbHelper.getRoomId()
        if(idRoomDef != -1L) {
            getRoomFromAPI(Room(idRoomDef, "", "",false,"","",""), true)
        }else{
            mainLayout.removeView(mainLayout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
            showUsersButton.visibility = View.INVISIBLE
            sortButton.visibility = View.INVISIBLE
            addButton.visibility = View.INVISIBLE
            dataPickerButton.visibility = View.INVISIBLE
            roomNameTextView.text = "Вы не в комнате. Попробуйте ее создать, найти или выберите нужную комнату"
        }

        changeScrollView()
    }


    private fun rebuildRoomPanel(){
        if(user!=null && user!!.availableRooms != "") {
            roomLayout.removeAllViews()
            val availableRooms: List<Int> = user!!.availableRooms.split("|").map { it.toInt() }.toMutableList()
            for (room in availableRooms)
            {
                val roomTextView = TextView(requireContext())
                roomTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // width
                    LinearLayout.LayoutParams.WRAP_CONTENT // height
                ).apply {
                    setMargins(5, 5, 5, 5) // left, top, right, bottom margins
                }
                roomTextView.id = room
                roomTextView.setBackgroundResource(R.drawable.border_room)
                roomManger.getRoom(room.toLong(),object :GetRoomCallback{
                    override fun onSuccess(gotRoom: Room) {
                        val text = "Название: ${gotRoom.name}\nНомер: ${gotRoom.idRoom}\nПароль: ${unHashPassword(gotRoom.password)}"
                        roomTextView.text = text
                        roomTextView.textSize = textSize
                        roomLayout.addView(roomTextView)

                        roomTextView.setOnClickListener{
                            idRoomDef = roomTextView.id.toLong()
                            dbHelper.updateRoomId(idRoomDef)
                            rebuildPage()
                        }
                    }

                    override fun onFailure(message: String) {}
                })

            }
        }
    }

    @SuppressLint("ResourceType")
    private fun showUsersPanel(){
        userScrView.visibility = View.VISIBLE
        val usersInRoom: List<String> = nowRoom.users.splitToSequence("|").toMutableList()

        showUsersButton.setOnClickListener{
            hideUsersPanel()
        }

        if(usersInRoom.isEmpty() || usersInRoom[0] == ""){
            val textView = createTextView("Здесь еще никого нет...")
            usersPanel.addView(textView)
            return
        }

        val textView = createTextView("Всего участников: " + (usersInRoom.count() + 1).toString())
        textView.id = 111111
        textView.setTextColor(Color.GRAY)
        textView.textSize = 15.0f
        textView.setPadding(10,0,0,0)

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        textView.setLayoutParams(params)

        val params2 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params2.addRule(RelativeLayout.BELOW,textView.id)


        val textView2 = createTextView("Главный: " + nowRoom.owner)
        textView2.id = 9988
        textView2.setPadding(10,5,10,5)
        textView2.setLayoutParams(params2)
        usersPanel.addView(textView)
        usersPanel.addView(textView2)

        if(user!!.login == nowRoom.owner) {
            textView2.text = "Вы главный"
            for (user in usersInRoom) {
                val button = ImageButton(requireContext())
                val textView = createTextView(user)
                button.setImageResource(R.drawable.ic_settings)
                button.id = 888888 + usersInRoom.indexOf(user)
                textView.id = 998899 + usersInRoom.indexOf(user)
                val btnParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

                if (usersInRoom.indexOf(user) == 0) {
                    btnParams.addRule(RelativeLayout.BELOW, 9988)
                } else {
                    btnParams.addRule(RelativeLayout.BELOW, 998899 + usersInRoom.indexOf(user) - 1)
                }

                button.setPadding(5, 5, 5, 5)
                btnParams.setMargins(5, 5, 5, 5)
                button.setLayoutParams(btnParams)
                usersPanel.addView(button)
                button.setOnClickListener {
                    showUserActionDialog(user)
                }

                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

                if (usersInRoom.indexOf(user) == 0) {
                    params.addRule(RelativeLayout.BELOW, 9988)
                } else {
                    params.addRule(RelativeLayout.BELOW, 998899 + usersInRoom.indexOf(user) - 1)
                }

                params.addRule(RelativeLayout.RIGHT_OF, 888888 + usersInRoom.indexOf(user))
                textView.setPadding(5, 5, 5, 5)
                params.setMargins(5, 5, 5, 5)
                textView.setLayoutParams(params)
                usersPanel.addView(textView)
            }
        }
        else{
            for (user in usersInRoom) {
                val textView = createTextView(user)
                textView.id = 998899 + usersInRoom.indexOf(user)
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

                if (usersInRoom.indexOf(user) == 0) {
                    params.addRule(RelativeLayout.BELOW, 9988)
                } else {
                    params.addRule(RelativeLayout.BELOW, 998899 + usersInRoom.indexOf(user) - 1)
                }

                textView.setPadding(5, 5, 5, 5)
                params.setMargins(5, 5, 5, 5)
                textView.setLayoutParams(params)
                usersPanel.addView(textView)
            }
        }
    }

    private fun showUserActionDialog(userLogin: String) {
        // Создаем диалоговое окно
        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setTitle("Выберите действие")
        dialogBuilder.setMessage("Что вы хотите сделать с пользователем?")

        dialogBuilder.setPositiveButton("Выгнать") { dialog, _ ->
            kickUser(userLogin)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Заблокировать") { dialog, _ ->
            blockUser(userLogin)
            dialog.dismiss()
            //TODO: реализовать мехнику уведомления юзера об изгнании/блокировке
        }

        dialogBuilder.setNeutralButton("Отмена") { dialog, _ ->
            // Закрываем окно без выполнения действий
            dialog.dismiss()
        }

        // Показываем диалоговое окно
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun kickUser(userLogin: String){
        nowRoom.users = nowRoom.users
            .replaceFirst("^$userLogin\\|".toRegex(), "") // Удаляет, если userLogin в начале
            .replaceFirst("\\|$userLogin".toRegex(), "")  // Удаляет, если userLogin в начале
            .replaceFirst(userLogin, "") // Удаляет, если userLogin где-то в середине
        updateRoomForAPI(nowRoom)

        userManager.getUser(userLogin, object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                gotUser.availableRooms = gotUser.availableRooms
                    .replaceFirst("^$idRoomDef\\|".toRegex(), "")
                    .replaceFirst("\\|$idRoomDef".toRegex(), "")
                    .replaceFirst(idRoomDef.toString(), "")
                updateUserForAPI(gotUser)
            }

            override fun onFailure(isExist: Boolean) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка! Пользователь не найден",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        rebuildUsersPanel()
    }

    private fun blockUser(userLogin: String){
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Вы уверены, что хотите заблокировать?")
        dialogBuilder.setPositiveButton("Да") { dialog, _ ->

            if(nowRoom.bannedUsers != "")
                nowRoom.bannedUsers = nowRoom.bannedUsers + "|" + userLogin
            else
                nowRoom.bannedUsers = userLogin

            kickUser(userLogin)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    private fun hideUsersPanel(){
        usersPanel.removeAllViews()
        userScrView.visibility = View.GONE
        showUsersButton.setOnClickListener{
            showUsersPanel()
        }
    }

    private fun rebuildUsersPanel(){
        usersPanel.removeAllViews()
        showUsersPanel()
    }

    private fun showRoomPanel(){
        dataPickerButton.visibility = View.INVISIBLE
        addButton.visibility = View.INVISIBLE

        createTaskPanel.visibility = View.INVISIBLE
        createEventPanel.visibility = View.INVISIBLE
        createRoomPanel.visibility = View.INVISIBLE
        addRoomPanel.visibility = View.INVISIBLE

        showRoomPanel.visibility = View.VISIBLE
    }

    private fun hideRoomPanel(){
        if (idRoomDef != (-1).toLong()){
            addButton.visibility = View.VISIBLE
            dataPickerButton.visibility = View.VISIBLE
        }
        showRoomPanel.visibility = View.GONE
        createRoomPanel.visibility = View.GONE
        addRoomPanel.visibility = View.GONE
        createNameRoom.setText("")
        createPasswordRoom.setText("")
        addIdRoom.setText("")
        addPasswordRoom.setText("")
    }

    private fun createRoom(){
        val newRoom = Room(
            0,
            createNameRoom.text.toString().trim(),
            hashPassword(createPasswordRoom.text.toString().trim()),
            false,
            user!!.login,
            "",
            ""
        )
        if(newRoom.name.length > 25) {
            createError("Слишком большое название комнаты")
            return
        }
        else if(createPasswordRoom.text.toString().trim().isEmpty() || createPasswordRoom.text.toString().trim().trim() == ""){
            createError("Ошибка! Нет пароля!")
            return
        }
        else if(createPasswordRoom.text.toString().trim().length <= 8){
            createError("Недостаточная длина пароля! Минимально 9 символов!")
            return
        }
        else{
            createRoomForAPI(newRoom)
        }
    }

    private fun addRoom(){
        val newRoom = Room(
            addIdRoom.text.toString().trim().toLong(),
            "",
            addPasswordRoom.text.toString().trim(),
            false,
            "",
            "",
            ""
        )
        var availableRooms: List<Long> = ArrayList()
        if(user!!.availableRooms != "") {
            availableRooms =
                user!!.availableRooms.split("|").map { it.toLong() }.toMutableList()
        }
        if(!availableRooms.contains(newRoom.idRoom)){
            getRoomFromAPI(newRoom,false)
        }

        else{
            hideRoomPanel()
            createError("Такая комната вам уже известна")
        }
    }

    private fun editRoom(){
        roomTextView.text = "Редактирование комнаты"
        createNameRoom.setText(nowRoom.name)
        createPasswordRoom.setText(nowRoom.password)
        saveRoomButton.setOnClickListener{
            if(createNameRoom.text.toString().isEmpty()){
                createError("Название забыли")
            }
            else if(createPasswordRoom.text.toString().length < 8){
                createError("Малая длина пароля")
            }
            else{
                rebuildRoomPanel()
                nowRoom.name = createNameRoom.text.toString().trim()
                nowRoom.password = createPasswordRoom.text.toString().trim()
                updateRoomForAPI(nowRoom)
                roomNameTextView.text = nowRoom.name
                hideRoom()
            }
        }
    }

    override fun setListeners(view: View){
        setTouchListenerForButtons(view)
        setTouchListenerForButtons(mainLayout)
        setTouchListenerForButtons(requireView().findViewById(R.id.scrView))
        setTouchListenerForButtons(showRoomPanelButton)
        setTouchListenerForButtons(roomNameTextView)
        setTouchListenerForButtons(requireView().findViewById(R.id.conLayout))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setTouchListenerForButtons(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deleteButton.visibility = View.GONE
                editButton.visibility = View.GONE
                calendarView.visibility = View.GONE
                mainCalendarView.visibility = View.GONE
                hideRoomPanel()
            }
            false
        }
    }

    private  fun hideRoom(){
        createRoomPanel.visibility = View.GONE
        roomTextView.text = "Создание комнаты"
        saveRoomButton.setOnClickListener {
            createRoom()
            createRoomButton.isEnabled = true
            addRoomButton.isEnabled = true
        }
        createRoomButton.isEnabled = true
        addRoomButton.isEnabled = true
        createNameRoom.setText("")
        createPasswordRoom.setText("")
    }
}