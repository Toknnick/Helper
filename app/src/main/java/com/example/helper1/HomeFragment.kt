package com.example.helper1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.fragment.app.Fragment
import com.example.helper1.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

interface Timable {
    var time: String
}

data class Event(
    var data: String,
    override var time: String,
    var place: String,
    var event: String
): Timable

data class Task(
    var data: String,
    override var time: String,
    var name: String,
    var points: List<String>,
    var checkBoxes: List<Boolean>
): Timable

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var deleteButton : Button
    private var chosenDate: String = ""
    private var events: List<Event> = ArrayList()
    private var tasks: List<Task> = ArrayList()
    private var countOfPoint = 1

    private val ENENT_ID = 10000
    private val TASK_ID = 232320
    private val REL_LAYOUT_ID = 145632223
    private val TEXT_VIEW_NOTHING_TO_DO_ID = 11111111

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        dbHelper.updateChosenDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        //requireContext().deleteDatabase(dbHelper.databaseName)
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
        initDatePicker()

        binding.addButton.setOnClickListener {
            showDialog()
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
        binding.dataPickerButton.setOnClickListener {
            datePickerDialog.show()
        }
        binding.backTaskButton.setOnClickListener {
            hideTaskPanel()
        }
        binding.backEventButton.setOnClickListener {
            hideEventPanel()
        }

        changeDataEditText(binding.dateInput)
        changeDataEditText(binding.dateTaskInput)
        changeTimeEditText(binding.timeInput)
        changeTimeEditText(binding.timeTaskInout)
    }

    override fun onResume() {
        super.onResume()
        chosenDate = dbHelper.getChosenDate()
        binding.dataPickerButton.text = chosenDate
        changeScrollView()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    private fun createNewText(item: Int) {
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
        if (!isHasDate(binding.dateInput.text.toString())) {
            createError("Ошибка! Некорректная дата!")
            return
        }

        if (!isHasTime(binding.timeInput.text.toString())) {
            createError("Ошибка! Некорректное время!")
            return
        }

        if (binding.eventInput.text.toString().trim().isEmpty()) {
            createError("Ошибка! Нет описания!")
            return
        }

        var i = 0
        if (events.isNotEmpty())
            i = events.size
        //Сохраняем в БД
        val event = Event(
            stringToDate(binding.dateInput.text.toString()),
            stringToTime(binding.timeInput.text.toString()),
            binding.placeInput.text.toString(),
            binding.eventInput.text.toString()
        )

        createError("Созданно на " + event.data)

        if (chosenDate == event.data) {
            events += event
            createAllEventsAndTasks()
        }

        hideEventPanel()
        dbHelper.insertEvent(event)
    }

    private fun addNewTaskIntoScrollView() {
        if (!isHasDate(binding.dateTaskInput.text.toString())) {
            createError("Ошибка! Некорректная дата!")
            return
        }

        if (!isHasTime(binding.timeTaskInout.text.toString())) {
            createError("Ошибка! Некорректное время!")
            return
        }

        var i = 1
        if (binding.point0.text.toString().trim().isEmpty()) {
            createError("Ошибка! У вас есть пустой пункт!")
            return
        }
        while (countOfPoint > i) {
            if (countOfPoint > 1 && binding.pointsPlace.findViewById<EditText>(i + TASK_ID).text.toString()
                    .trim().isEmpty()
            ) {
                createError("Ошибка! У вас есть пустой пункт!")
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
                points += binding.pointsPlace.findViewById<EditText>(j + TASK_ID + 1).text.toString()
                    .trim()
                checkBoxes += false
            }
            j += 1
        }

        if (binding.timeInput.text.toString().trim().isEmpty()) {
            binding.timeInput.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
        }

        //Сохраняем в БД
        val task = Task(
            stringToDate(binding.dateTaskInput.text.toString()),
            stringToTime(binding.timeTaskInout.text.toString()),
            binding.nameTaskInput.text.toString(),
            points,
            checkBoxes
        )

        if (chosenDate == task.data){
            tasks += task
            createAllEventsAndTasks()
        }

        createError("Созданно на " + task.data)
        hideTaskPanel()
        dbHelper.insertTask(task)
    }

    @SuppressLint("ResourceType")
    private fun createNewTask(points: List<String>, checkBoxes: List<Boolean>, i: Int) {
        if (binding.layout.findViewById<TextView>(TEXT_VIEW_NOTHING_TO_DO_ID) != null) {
            binding.layout.removeView(binding.layout.findViewById(TEXT_VIEW_NOTHING_TO_DO_ID))
        }

        val layout = createRelativeLayout(i)
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
            addParamsToNewPoint(textView, layout, j, checkBoxes[j])
            j += 1
        }

        layout.setBackgroundResource(R.drawable.border_task)
        binding.layout.addView(layout)
        setupLongClickListeners(layout,i)
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
                        events[i].event,false, true)
        } else if (events[i].time.length<7) {
            textView = createText(
                "Время: " + events[i].time + System.lineSeparator() +
                        events[i].event,false, true)
        } else {
            textView = createText(events[i].event,false,true)
        }

        textView.setBackgroundResource(R.drawable.border_event)
        textView.id = i + ENENT_ID
        binding.layout.addView(textView)
        setupLongClickListeners(textView,i)
    }

    private fun addNewPoint() {
        val editText = EditText(context)
        //Подвинуть пункт
        addParamsToEditText(editText)
        //Подвинуть кнопки
        addParamsToButtons(editText)
        countOfPoint += 1
    }

    //TODO: добавить возможность менять цветa в настройках
    @SuppressLint("ResourceAsColor")
    private fun createText(text: String, isNameText: Boolean = false, isNeedBelow:Boolean = false): TextView {
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

        if(isNeedBelow) {
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
        textView.setTextColor(R.color.text_color_button)
        textView.setLayoutParams(params)
        textView.text = text
        textView.textSize = 22F
        return textView
    }

    @SuppressLint("ResourceAsColor")
    private fun createButton(){
        deleteButton = Button(requireContext())
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)
        deleteButton.setLayoutParams(params)
        deleteButton.setBackgroundResource(R.color.bottom_nav_bg)
        deleteButton.setTextColor(R.color.text_color_button)
        deleteButton.text = "Удалить"
        deleteButton.textSize = 22F
        deleteButton.visibility = View.GONE
    }

    private fun createRelativeLayout(id: Int): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.id = REL_LAYOUT_ID + id
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)


        if(binding.layout.childCount == 0 || (binding.layout.childCount == 1 && binding.layout.getChildAt(0) == deleteButton)){
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
        }else if(binding.layout.getChildAt(binding.layout.childCount - 1) != deleteButton){
            params.addRule(RelativeLayout.BELOW,
                           binding.layout.getChildAt(binding.layout.childCount - 1).id)
        }else{
            params.addRule(RelativeLayout.BELOW,
                binding.layout.getChildAt(binding.layout.childCount - 2).id)
        }

        layout.setLayoutParams(params)
        layout.setPadding(0, 0, 0, 10)
        return layout
    }

    @SuppressLint("ResourceAsColor")
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
        editText.setTextColor(R.color.text_color_button)

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
        if (!isChecked)
            textView.setBackgroundResource(R.drawable.border_not_completed_task)
        else
            textView.setBackgroundResource(R.drawable.border_completed_task)

        textView.id = j + 121210
        checkBoxParams.addRule(RelativeLayout.RIGHT_OF, textView.id)
        checkBox.setLayoutParams(checkBoxParams)

        checkBox.setOnClickListener {
            changeBackgroundOfPoint(textView, checkBox, relLayout.id - REL_LAYOUT_ID , j)
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
        btn3Params.rightMargin = 10
        btn3Params.addRule(RelativeLayout.LEFT_OF, binding.addNewPoint.id)
        binding.deletePoint.setLayoutParams(btn3Params)
    }

    private fun changeBackgroundOfPoint(textView: TextView, checkBox: CheckBox, i: Int, j: Int) {
        val newCheckBoxes = tasks[i].checkBoxes.toMutableList()

        if (checkBox.isChecked) {
            textView.setBackgroundResource(R.drawable.border_completed_task)
            newCheckBoxes[j] = true
        } else {
            textView.setBackgroundResource(R.drawable.border_not_completed_task)
            newCheckBoxes[j] = false
        }

        tasks[i].checkBoxes = newCheckBoxes
        dbHelper.updateTaskCheckBoxes(tasks[i], newCheckBoxes)
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

    private fun clearEditText(editText: EditText): EditText {
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
            createError("Ошибка! Нельзя удалить этот пункт!")
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
                if (text.length == 8) {
                    val month = text.substring(3, 5).toIntOrNull()
                    if (month != null && month > 12) {
                        editText.setText(text.substring(0, 3) + "12.")
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

    private fun createError(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun isHasDate(date: String): Boolean {
        var boolean = false
        if (date.length == 8 || date.length == 10 || date.isEmpty())
            boolean = true

        return boolean
    }

    private fun isHasTime(time: String): Boolean {
        var boolean = false
        if (time.length == 5 || time.isEmpty())
            boolean = true

        return boolean
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
        var newString = ""

        if (string.length == 8)
            newString = string.replace(Regex("(\\d{2})$"), "20$1")
        else if (string.isEmpty())
            newString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        return newString
    }

    private fun stringToTime(string: String) : String{
        var newString = ""
        if (string.isEmpty())
            newString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        else
            newString = string

        return newString
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLongClickListeners(view: View,id: Int){
        view.setOnLongClickListener{
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 5, 5, 5)

            if (binding.layout.indexOfChild(view) == 0){
                params.addRule(RelativeLayout.BELOW,view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            }else if(binding.layout.indexOfChild(view) == binding.layout.childCount -1
                ||(binding.layout.indexOfChild(view) == binding.layout.childCount - 2 && binding.layout.getChildAt(binding.layout.childCount- 1) == deleteButton)){
                params.addRule(RelativeLayout.ABOVE,view.id)
                params.addRule(RelativeLayout.ALIGN_LEFT, view.id)
            }
            deleteButton.setLayoutParams(params)
            deleteButton.visibility = View.VISIBLE

        deleteButton.setOnClickListener {
            when(view){
                is TextView -> {
                    dbHelper.deleteEvent(events[id])
                }
                is RelativeLayout ->{
                    dbHelper.deleteTask(tasks[id])
                }
            }

            binding.layout.removeView(view)
            checkToNothingToDo()
            changeScrollView()
            deleteButton.visibility = View.GONE
        }
            true
        }

        setTouchListener(view,deleteButton)
        setTouchListener(binding.layout, deleteButton)
        setTouchListener(binding.conLayout, deleteButton)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(view: View, deleteButton: Button) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deleteButton.visibility = View.GONE
            }
            false
        }
    }

    private fun createAllEventsAndTasks() {
        createButton()

        val newList = (events + tasks).sortedBy { it.time }
        for (item in newList) {
            when (item) {
                is Event -> createNewEvent(events.indexOf(item))
                is Task -> createNewTask(item.points,item.checkBoxes,tasks.indexOf(item))
            }
        }
        binding.layout.addView(deleteButton)
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
        events = dbHelper.getEventsByDate(chosenDate).sortedBy { it.time }
        tasks = dbHelper.getTasksByDate(chosenDate).sortedBy { it.time }
        binding.layout.removeAllViews()
        createAllEventsAndTasks()
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION", "NAME_SHADOWING")
    private fun initDatePicker() {
        val dateSetListener =
            OnDateSetListener { _, year, month, day ->
                var month = month
                month += 1
                val date: String

                if (month > 9)
                    date = "$day.$month.$year"
                else
                    date = "$day.0$month.$year"

                binding.dataPickerButton.text = date
                chosenDate = date
                dbHelper.updateChosenDate(chosenDate)
                changeScrollView()
            }

        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)

        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        datePickerDialog =
            DatePickerDialog(requireContext(), style, dateSetListener, year, month, day)
    }

    private fun showDialog() {
        val langArray: Array<String> = arrayOf("Задача", "Событие")
        val selectedEvent = BooleanArray(langArray.size)
        val langList: ArrayList<Int> = ArrayList()
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Выбрерите тип")
        builder.setCancelable(false)

        builder.setMultiChoiceItems(
            langArray, selectedEvent
        ) { _, i, b ->
            if (b) {
                langList.add(i)
                langList.sort()
            } else {
                langList.remove(i)
            }
        }

        builder.setPositiveButton(
            "OK"
        ) { _, _ -> // Initialize string builder
            if (langList.size != 1) {
                val stringBuilder = StringBuilder()
                for (j in langList.indices) {
                    stringBuilder.append(langArray[langList[j]])
                    if (j != langList.size - 1) {
                        stringBuilder.append(", ")
                    }
                }
                createError("Ошибка! Необходимо выбрать что-то одно")
            } else {
                createNewText(langList[0])
            }
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, _ -> // dismiss dialog
            dialogInterface.dismiss()
        }

        builder.show()
    }
}