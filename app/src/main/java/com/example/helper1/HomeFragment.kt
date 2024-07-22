package com.example.helper1

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.helper1.databinding.FragmentHomeBinding
import android.text.Editable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Event(
    var data: String,
    var time: String,
    var place: String,
    var event: String
)

data class Task(
    var data: String,
    var time: String,
    var name: String,
    var points: List<String>,
    var checkBoxes : List<Boolean>
)

class HomeFragment :  Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private var events: List<Event> = ArrayList()
    private var tasks: List<Task> = ArrayList()
    private var countOfPoint = 1

    private val ENENT_ID = 10000
    private val TASK_ID = 232320

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        //requireContext().deleteDatabase(dbHelper.databaseName)
        events = dbHelper.getEvents()
        tasks = dbHelper.getTasks()
    }

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

        binding.addButton.setOnClickListener {
            mainActivity.showDialog(this)
        }
        binding.saveButton.setOnClickListener {
            addNewEventIntoScrollView()
        }
        binding.saveTask.setOnClickListener {
            addNewTaskIntoScrollView()
        }
        binding.addNewPoint.setOnClickListener {
            addNewPoint()
        }
        binding.deletePoint.setOnClickListener {
            deletePoint()
        }


        changeDataEditText(binding.dateInput)
        changeDataEditText(binding.dateTaskInput)
        changeTimeEditText(binding.timeInput)
        changeTimeEditText(binding.timeTaskInout)
    }

    override fun onResume() {
        super.onResume()
        createAllEventsAndTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val tasssskkkkssss = dbHelper.getTasks()
        val eventssssssss = dbHelper.getEvents()
        //dbHelper.close()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun createNewText(item: Int) {
        if (item == 0) {
            //Делаем задачу
            clearTaskPanel()
            countOfPoint = 1
            binding.createTaskPanel.visibility = View.VISIBLE
            binding.addButton.isEnabled = false
        } else {
            //Делаем событие
            clearEventPanel()
            binding.createEventPanel.visibility = View.VISIBLE
            binding.addButton.isEnabled = false
        }
    }

    private fun addNewEventIntoScrollView() {
        if(!isHasDate(binding.dateInput.text.toString())){
            mainActivity.createError("Ошибка! Некорректная дата!")
            return
        }

        if (!isHasTime(binding.timeInput.text.toString())){
            mainActivity.createError("Ошибка! Некорректное время!")
            return
        }

        var i = 0
        if (events.isNotEmpty())
            i = events.size

        //Сохраняем в БД
        val event = Event(
            stringToDate(binding.dateInput.text.toString()),
            binding.timeInput.text.toString(),
            binding.placeInput.text.toString(),
            binding.eventInput.text.toString()
        )
        events += event

        createNewEvent(i)
        dbHelper.insertEvent(event)

        binding.addButton.isEnabled = true
        binding.createEventPanel.visibility = View.GONE
    }

    private fun addNewTaskIntoScrollView() {
        if(!isHasDate(binding.dateTaskInput.text.toString())){
            mainActivity.createError("Ошибка! Некорректная дата!")
            return
        }

        if (!isHasTime(binding.timeTaskInout.text.toString())){
            mainActivity.createError("Ошибка! Некорректное время!")
            return
        }
        var i = 1

        while (countOfPoint > i) {
            if (binding.point0.text.toString().trim().isEmpty()) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            } else if (countOfPoint > 1 &&
                binding.pointsPlace.findViewById<EditText>(i + TASK_ID).text.toString().trim()
                    .isEmpty()
            ) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            }
            i += 1
        }

        //Добавляем пункты
        var points: List<String> = ArrayList()
        var checkBoxes: List<Boolean> = ArrayList()
        clearEditText(binding.point0)
        points += binding.point0.text.toString()
        checkBoxes += false

        var j = 0
        while (countOfPoint > j) {
            if (points.size != countOfPoint) {
                clearEditText(binding.pointsPlace.findViewById(j + TASK_ID + 1))
                points += binding.pointsPlace.findViewById<EditText>(j + TASK_ID + 1).text.toString().trim()
                checkBoxes += false
            }
            j += 1
        }

        //Сохраняем в БД
        val task = Task(
            stringToDate(binding.dateTaskInput.text.toString()),
            binding.timeTaskInout.text.toString(),
            binding.nameTaskInput.text.toString(),
            points,
            checkBoxes
        )
        tasks += task
        createNewTask(points,checkBoxes,tasks.size-1)
        dbHelper.insertTask(task)

        binding.addButton.isEnabled = true
        binding.createTaskPanel.visibility = View.GONE

    }

    @SuppressLint("ResourceType")
    private fun createNewTask(points: List<String>,checkBoxes : List<Boolean>, i:Int) {
        val layout = createRelativeLayout()
        val nameTextView: TextView

        if (tasks[i].name.isEmpty())
            nameTextView = createText("Нет названия")
        else
            nameTextView = createText(tasks[i].name, true)
        layout.addView(nameTextView)
        nameTextView.id = 666
        nameTextView.textSize = 23F
        nameTextView.gravity = Gravity.CENTER

        var j = 0
        while (points.size > j) {
            val textView = createText(points[j])
            addParamsToNewPoint(textView, layout, j,checkBoxes[j])
            j += 1
        }

        layout.setBackgroundResource(R.drawable.border_task)
        binding.layout.addView(layout)
    }

    private fun createNewEvent(i: Int) {
        val textView: TextView

        if (events[i].place != "") {
            textView = createText(
                "Время: " + events[i].time + System.lineSeparator() +
                        "Место: " + events[i].place + System.lineSeparator() +
                        events[i].event
            )
        } else if (events[i].time != "") {
            textView = createText(
                "Время: " + events[i].time + System.lineSeparator() +
                        events[i].event
            )
        } else {
            textView = createText(events[i].event)
        }

        textView.setBackgroundResource(R.drawable.border_event)
        textView.id = i + ENENT_ID
        binding.layout.addView(textView)
    }

    private fun addNewPoint() {
        val editText = EditText(context)
        //Подвинуть пункт
        addParamsToEditText(editText)
        //Подвинуть кнопки
        addParamsToButtons(editText)
        countOfPoint += 1
    }

    //TODO: добавить возможность менять цвет в настройках
    private fun createText(text: String, isNameText: Boolean = false): TextView {
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

        textView.setLayoutParams(params)
        textView.text = text
        textView.textSize = 22F
        return textView
    }

    private fun createRelativeLayout(): RelativeLayout {
        val layout = RelativeLayout(context)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 10)
        layout.setLayoutParams(params)
        layout.setPadding(0, 0, 0, 10)
        return layout
    }

    private fun addParamsToEditText(editText: EditText) {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        //Установить новый пункт
        params.setMargins(30, 30, 30, 30)
        editText.setLayoutParams(params)
        editText.setBackgroundResource(R.color.edit_text)
        editText.hint = "Пункт"
        editText.id = countOfPoint + TASK_ID
        editText.setPadding(10, 10, 10, 40)
        binding.pointsPlace.addView(editText)

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
    private fun addParamsToNewPoint(textView: TextView, relLayout: RelativeLayout, j: Int, isChecked : Boolean) {
        val checkBox = CheckBox(context)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val checkBoxParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        relLayout.addView(textView)
        if (j == 0) {
            params.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(666).id)
            checkBoxParams.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(666).id)
        } else {
            params.addRule(
                RelativeLayout.BELOW,
                relLayout.findViewById<TextView>(j + 121210 - 1).id
            )
            checkBoxParams.addRule(
                RelativeLayout.BELOW,
                relLayout.findViewById<TextView>(j + 121210 - 1).id
            )
        }

        params.setMargins(10, 10, 0, 10)
        textView.setLayoutParams(params)
        textView.maxWidth = (Resources.getSystem().displayMetrics.widthPixels * 0.9f).toInt()


        checkBox.isChecked = isChecked
        if(!isChecked)
            textView.setBackgroundResource(R.drawable.border_not_completed_task)
        else
            textView.setBackgroundResource(R.drawable.border_completed_task)

        textView.id = j + 121210
        checkBoxParams.addRule(RelativeLayout.RIGHT_OF, textView.id)
        checkBox.setLayoutParams(checkBoxParams)

        checkBox.setOnClickListener {
            changeBackgroundOfPoint(textView, checkBox,tasks.size-1,j)
        }

        relLayout.addView(checkBox)
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
        binding.saveTask.setLayoutParams(btn1Params)

        btn2Params.addRule(RelativeLayout.ALIGN_END, editText.id)
        btn2Params.addRule(RelativeLayout.BELOW, editText.id)
        binding.addNewPoint.setLayoutParams(btn2Params)

        btn3Params.addRule(RelativeLayout.BELOW, editText.id)
        //btn3Params.rightMargin = 20
        btn3Params.addRule(RelativeLayout.LEFT_OF, binding.addNewPoint.id)
        binding.deletePoint.setLayoutParams(btn3Params)
    }

    private fun changeBackgroundOfPoint(textView: TextView, checkBox: CheckBox, i: Int, j: Int) {
        val newCheckBoxes = tasks[i].checkBoxes.toMutableList()

        if (checkBox.isChecked) {
            textView.setBackgroundResource(R.drawable.border_completed_task)
            newCheckBoxes[j] = true
        }else{
            textView.setBackgroundResource(R.drawable.border_not_completed_task)
            newCheckBoxes[j] = false
        }

        tasks[i].checkBoxes = newCheckBoxes
        dbHelper.updateTaskCheckBoxes(i+1, newCheckBoxes)
    }

    private fun clearEventPanel() {
        binding.dateInput.setText("")
        binding.timeInput.setText("")
        binding.placeInput.setText("")
        binding.eventInput.setText("")
    }

    private fun clearTaskPanel() {
        binding.dateTaskInput.setText("")
        binding.timeTaskInout.setText("")
        binding.nameTaskInput.setText("")
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

    private fun clearEditText(editText: EditText): EditText{
        editText.text.toString().trim()
        return editText
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
            mainActivity.createError("Ошибка! Нельзя удалить этот пункт!")
        }
    }

    private fun changeTimeEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var wasDeleted = false

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                wasDeleted = count > after
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text.length == 2 && !text.contains(":") && !wasDeleted) {
                    editText.setText("$text:")
                    editText.setSelection(editText.text.length)
                }
                if (text.length > 5) {
                    editText.setText(text.substring(0, 5))
                    editText.setSelection(editText.text.length)
                }
                if (text.length > 3) {
                    val hours = text.substring(0, 2).toIntOrNull()
                    if (hours != null && hours > 23) {
                        editText.setText("23:")
                        editText.setSelection(editText.text.length)
                    }
                }
                if (text.length == 5) {
                    val minutes = text.substring(3, 5).toIntOrNull()
                    if (minutes != null && minutes > 59) {
                        editText.setText(text.substring(0, 3) + "59")
                        editText.setSelection(editText.text.length)
                    }
                }
                wasDeleted = false
            }
        })
    }

    private fun changeDataEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var wasDeleted = false

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                wasDeleted = count > after
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if ((text.length == 2 || text.length == 5) && !wasDeleted) {
                    editText.setText("$text.")
                    editText.setSelection(editText.text.length)
                }
                if (text.length > 10) {
                    editText.setText(text.substring(0, 10))
                    editText.setSelection(editText.text.length)
                }
                if (text.length == 4) {
                    val day = text.substring(0, 2).toIntOrNull()
                    if (day != null && day > 31) {
                        editText.setText("31.")
                        editText.setSelection(editText.text.length)
                    }
                }
                if (text.length == 7) {
                    val month = text.substring(3, 5).toIntOrNull()
                    if (month != null && month > 12) {
                        editText.setText(text.substring(0, 3) + "12.")
                        editText.setSelection(editText.text.length)
                    }else if(month != null && month < 10){
                        editText.setText(text.substring(0, 3) + "0" + month + ".")
                        editText.setSelection(editText.text.length)
                    }
                }
                if (text.length == 10) {
                    val year = text.substring(6, 10).toIntOrNull()
                    if (year != null && year < 1900) {
                        editText.setText(text.substring(0, 6) + "1900")
                        editText.setSelection(editText.text.length)
                    }
                }
                wasDeleted = false
            }
        })
    }

    private fun isHasDate(date:String): Boolean{
        var boolean = false
        if (date.length == 8 || date.length == 10 || date.isEmpty())
            boolean = true

        return boolean
    }

    private fun isHasTime(time:String): Boolean{
        var boolean = false
        if (time.length == 5 ||time.isEmpty())
            boolean = true

        return boolean
    }

    private fun stringToDate(string: String) : String{
        var newString= ""

        if (string.length == 8)
            newString = string.replace(Regex("(\\d{2})$"), "20$1")
        else if (string.length == 0)
            newString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        return newString
    }

    private fun createAllEventsAndTasks(){
        var i = 0
        while (i < tasks.size){
            createNewTask(tasks[i].points,tasks[i].checkBoxes,i)
            i+=1
        }

        i = 0
        while (i < events.size){
            createNewEvent(i)
            i+=1
        }
    }
}