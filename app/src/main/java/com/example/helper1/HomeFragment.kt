package com.example.helper1

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
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
    var points: List<Pair<String, Boolean>>
)

class HomeFragment :  Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private var events: List<Event> = ArrayList()
    private var tasks: List<Task> = ArrayList()
    private var countOfPoint = 1

    private val ENENT_ID = 10000
    private val TASK_ID = 232320

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = (activity as MainActivity)
        addParamToButtons(binding.point0)

        binding.addButton.setOnClickListener{
            mainActivity.showDialog(this)
        }
        binding.saveButton.setOnClickListener{
            addNewEventIntoScrollView()
        }
        binding.saveTask.setOnClickListener{
            addNewTaskIntoScrollView()
        }
        binding.addNewPoint.setOnClickListener{
            addNewPoint()
        }
        binding.deletePoint.setOnClickListener{
            deletePoint()
        }
    }

    fun createNewText(item: Int){
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

    private fun addNewEventIntoScrollView(){
        var i = 0
        if (events.isNotEmpty())
            i = events.size

        //Сохраняем в БД
        events += Event(binding.dateInput.text.toString(), binding.timeInput.text.toString(),
            binding.placeInput.text.toString(), binding.eventInput.text.toString())

        createNewEvent(i)

        binding.addButton.isEnabled = true
        binding.createEventPanel.visibility = View.GONE
    }

    private fun addNewTaskIntoScrollView(){
        var i = 1
        
        while (countOfPoint > i) {
            if(binding.point0.text.toString().trim().isEmpty()) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            }else if (countOfPoint > 1 &&
                binding.pointsPlace.findViewById<EditText>(i + TASK_ID).text.toString().trim().isEmpty()) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            }
            i+=1
        }

        //Добавляем пункты
        var points: List<Pair<String,Boolean>> = ArrayList()
        points += Pair(binding.point0.text.toString(),false)

        var j = 0
        while (countOfPoint > j){
            if(points.size != countOfPoint)
                points += Pair(binding.pointsPlace.findViewById<EditText>(j + TASK_ID +1).text.toString(),false)
            j += 1
        }
        
        //Сохраняем в БД
        tasks += Task(binding.dateTaskInput.text.toString(), binding.timeInput.text.toString(),
            binding.nameTaskInput.text.toString(),points)
        createNewTask(points)
        binding.addButton.isEnabled = true
        binding.createTaskPanel.visibility = View.GONE

    }

    @SuppressLint("ResourceType")
    private fun createNewTask(points: List<Pair<String,Boolean>>){
        val layout = createRelativeLayout()
        val nameTextView:TextView

        if (binding.nameTaskInput.text.isEmpty())
            nameTextView = createText("Нет названия")
        else
            nameTextView = createText(binding.nameTaskInput.text.toString(),true)
        layout.addView(nameTextView)
        nameTextView.id = 666
        nameTextView.textSize = 23F
        nameTextView.gravity = Gravity.CENTER

        var j = 0
        while (countOfPoint > j){
            val textView = createText(points[j].first)
            addParamsToNewPoint(textView,layout,j)
            j += 1
        }

        layout.setBackgroundResource(R.drawable.border_task)
        binding.layout.addView(layout)
    }

    private fun createNewEvent(i: Int) {
        val textView:TextView

        if (events[i].place != ""){
            textView= createText("Время: " + events[i].time + System.lineSeparator() +
                                                   "Место: " + events[i].place + System.lineSeparator() +
                                                               events[i].event)
        }else if (events[i].time != ""){
            textView = createText("Время: " +events[i].time  + System.lineSeparator() +
                                                               events[i].event)
        }else {
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
        addParamToButtons(editText)
        countOfPoint +=1
    }

    //TODO: добавить возможность менять цвет в настройки
    private fun createText(text: String, isNameText: Boolean = false): TextView {
        val textView = TextView(context)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(15, 15, 15, 15)

        if(isNameText) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            params.setMargins(1, 1, 1, 1)
        }
        
        textView.setLayoutParams(params)
        textView.text = text
        textView.textSize = 22F
        return textView
    }

    private fun createRelativeLayout() : RelativeLayout{
        val layout = RelativeLayout(context)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(5,5,5,10)
        layout.setLayoutParams(params)
        layout.setPadding(0,0,0,10)
        return layout
    }

    private  fun addParamsToEditText(editText: EditText){
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        //Установить новый пункт
        params.setMargins(30,30,30,30)
        editText.setLayoutParams(params)
        editText.setBackgroundResource(R.color.edit_text)
        editText.hint = "Пункт"
        editText.id = countOfPoint + TASK_ID
        editText.setPadding(10,10,10,40)
        binding.pointsPlace.addView(editText)

        //Подвинуть новый пункт
        if(countOfPoint == 1) {
            params.addRule(RelativeLayout.BELOW, binding.point0.id)
        }else{
            params.addRule(RelativeLayout.BELOW, binding.createTaskPanel.findViewById<EditText>(countOfPoint + TASK_ID-1).id)
        }
        editText.setLayoutParams(params)
    }

    @SuppressLint("ResourceType")
    private fun addParamsToNewPoint(textView: TextView, relLayout: RelativeLayout, j:Int){
        val checkBox = CheckBox(context)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        val checkBoxParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                         RelativeLayout.LayoutParams.WRAP_CONTENT)

        relLayout.addView(textView)
        if (j == 0){
            params.addRule(RelativeLayout.BELOW,relLayout.findViewById<TextView>(666).id)
            checkBoxParams.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(666).id)
        }else{
            params.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(j + 121210-1).id)
            checkBoxParams.addRule(RelativeLayout.BELOW, relLayout.findViewById<TextView>(j + 121210-1).id)
        }

        params.setMargins(10, 10, 0, 10)
        textView.setLayoutParams(params)
        textView.maxWidth = (Resources.getSystem().displayMetrics.widthPixels*0.9f).toInt()
        textView.setBackgroundResource(R.drawable.border_not_completed_task)
        textView.id = j + 121210

        checkBoxParams.addRule(RelativeLayout.RIGHT_OF,textView.id)
        checkBox.setLayoutParams(checkBoxParams)

        checkBox.setOnClickListener{
                changeBackgroundOfPoint(textView,checkBox)
        }

        relLayout.addView(checkBox)
    }

    private fun changeBackgroundOfPoint(textView: TextView, checkBox: CheckBox){
        if(checkBox.isChecked)
            textView.setBackgroundResource(R.drawable.border_completed_task)
        else
            textView.setBackgroundResource(R.drawable.border_not_completed_task)
    }

    private fun addParamToButtons(editText: EditText) {
        val btn1Params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                     RelativeLayout.LayoutParams.WRAP_CONTENT)
        val btn2Params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                     RelativeLayout.LayoutParams.WRAP_CONTENT)
        val btn3Params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                     RelativeLayout.LayoutParams.WRAP_CONTENT)

        btn1Params.addRule(RelativeLayout.ALIGN_START,editText.id)
        btn1Params.addRule(RelativeLayout.BELOW, editText.id)
        binding.saveTask.setLayoutParams(btn1Params)

        btn2Params.addRule(RelativeLayout.ALIGN_END,editText.id)
        btn2Params.addRule(RelativeLayout.BELOW, editText.id)
        binding.addNewPoint.setLayoutParams(btn2Params)

        btn3Params.addRule(RelativeLayout.BELOW, editText.id)
        btn3Params.rightMargin = 20
        btn3Params.addRule(RelativeLayout.LEFT_OF, binding.addNewPoint.id)
        binding.deletePoint.setLayoutParams(btn3Params)
    }

    private fun clearEventPanel(){
        binding.dateInput.setText("")
        binding.timeInput.setText("")
        binding.placeInput.setText("")
        binding.eventInput.setText("")
    }

    private fun clearTaskPanel(){
        binding.dateTaskInput.setText("")
        binding.timeTaskInout.setText("")
        binding.nameTaskInput.setText("")
        binding.point0.setText("")
        addParamToButtons(binding.point0)

        while (countOfPoint > 1){
            binding.pointsPlace.removeView(binding.createTaskPanel.findViewById<EditText>(countOfPoint + TASK_ID-1))
            countOfPoint -= 1
        }
    }

    private fun deletePoint() {
        if (countOfPoint > 2) {
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID-1))
            countOfPoint -= 1
            addParamToButtons(binding.pointsPlace.findViewById(countOfPoint + TASK_ID-1))
        }else if(countOfPoint > 1){
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + TASK_ID-1))
            countOfPoint -= 1
            addParamToButtons(binding.point0)
        }else{
            mainActivity.createError("Ошибка! Нельзя удалить этот пункт!")
        }
    }
}