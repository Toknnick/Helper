package com.example.helper1.fragments

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.helper1.R
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.CreateRoomCallback
import com.example.helper1.dataBase.GetAllRoomsCallback
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.User

@Suppress("NAME_SHADOWING", "DEPRECATION")
class RoomsFragment : ParentFragment() {
    //TODO: перенести метод с обновлением пароля пользователя в settingsFragment

    override fun setUpButtons() {

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
            createRoomPanel.visibility = View.GONE
            createRoomButton.isEnabled = true
            addRoomButton.isEnabled = true
            createNameRoom.setText("")
            createPasswordRoom.setText("")
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
                val idRoom = (rooms.count() + 1).toLong()
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
                if(gotRoom.single == false) {
                    if (isForText) {
                        roomNameTextView.text = gotRoom.name
                        addButton.visibility = View.VISIBLE
                        dataPickerButton.visibility = View.VISIBLE
                    } else {
                        if (gettingRoom.password == gotRoom.password) {
                            if (user!!.availableRooms != "") {
                                user!!.availableRooms += "|${gettingRoom.idRoom}"
                            } else {
                                user!!.availableRooms += "${gettingRoom.idRoom}"
                            }
                            idRoomDef = gettingRoom.idRoom
                            rebuildRoomPanel()
                            updateUserForAPI(user!!)
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

    private fun updateUserForAPI(newUser: User) {
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        if(idRoomDef != (-1).toLong()) {
            getRoomFromAPI(Room(idRoomDef, "", "",false), true)
        }else{
            addButton.visibility = View.INVISIBLE
            dataPickerButton.visibility = View.INVISIBLE
            roomNameTextView.text = "У вас еще нет ни одной комнаты. Попробуйте ее создать или найти"
        }
        changeScrollView()
    }


    private fun rebuildRoomPanel(){
        if(user!=null && user!!.availableRooms != "") {
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
                        val text = "${gotRoom.name}\nНомер: ${gotRoom.idRoom}\nПароль: ${gotRoom.password}"
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
        showRoomPanel.visibility = View.GONE
        createRoomPanel.visibility = View.GONE
        addRoomPanel.visibility = View.GONE
        addButton.visibility = View.VISIBLE
        dataPickerButton.visibility = View.VISIBLE
        createNameRoom.setText("")
        createPasswordRoom.setText("")
        addIdRoom.setText("")
        addPasswordRoom.setText("")
    }

    private fun createRoom(){
        val newRoom = Room(
            0,
            createNameRoom.text.toString().trim(),
            createPasswordRoom.text.toString().trim(),
            false
        )
        if(newRoom.name.length <= 25) {
            createRoomForAPI(newRoom)
        }
        else if(newRoom.password.isEmpty() || newRoom.password == ""){
            createError("Ошибка! Нет пароля!")
        }
        else{
            createError("Слишком большое название!")
        }
    }

    private fun addRoom(){
        val newRoom = Room(
            addIdRoom.text.toString().trim().toLong(),
            "",
            addPasswordRoom.text.toString().trim(),
            false
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
}