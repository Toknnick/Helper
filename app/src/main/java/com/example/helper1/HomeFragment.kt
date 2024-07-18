package com.example.helper1

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.PixelCopy.Request
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.Orientation
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
    var points: List<String>
)

class HomeFragment :  Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var events: List<Event>
    private lateinit var tasks: List<Task>
    private var countOfPoint = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        events = ArrayList()
        tasks = ArrayList()
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

    private fun addNewPoint() {
        val editText = mainActivity.createEditText()
        //Подвинуть пункт
        addParamsToEditText(editText)
        //Подвинуть кнопки
        addParamToButtons(editText)
        countOfPoint +=1
    }

    @SuppressLint("ResourceType")
    private fun addNewTaskIntoScrollView(){
        if(binding.nameTaskInput.text.toString().trim().isEmpty()){
            mainActivity.createError("Ошибка! Нет названия!")
            return
        }

        var i = 1
        while (countOfPoint > i) {
            if(binding.point0.text.toString().trim().isEmpty()) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            }else if (countOfPoint > 1 &&
                      binding.pointsPlace.findViewById<EditText>(i + 232320).text.toString().trim().isEmpty()) {
                mainActivity.createError("Ошибка! У вас есть пустой пункт!")
                return
            }
            i+=1
        }

        val layout = mainActivity.createRelativeLayout()

        //Добавляем заголовок
        val nameTextView:TextView
        if (binding.nameTaskInput.text.isEmpty())
            nameTextView = mainActivity.createText("Нет названия!")
        else
            nameTextView = mainActivity.createText(binding.nameTaskInput.text.toString())
        val nameTextViewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                             RelativeLayout.LayoutParams.WRAP_CONTENT)
        nameTextViewParams.setMargins(15, 15, 15, 15)
        nameTextViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        nameTextViewParams.setMargins(1,1,1,1)
        nameTextView.setLayoutParams(nameTextViewParams)
        layout.addView(nameTextView)
        nameTextView.id = 666
        nameTextView.textSize = 23F
        nameTextView.gravity = Gravity.CENTER

        //Добавляем пункты
        var points: List<String> = ArrayList()
        var j = 0
        points += binding.point0.text.toString()

        while (countOfPoint > j){
            if(points.size != countOfPoint)
                points += binding.pointsPlace.findViewById<EditText>(j + 232320+1).text.toString()

            val textView = mainActivity.createText(points[j])
            addParamsToNewPoint(textView,layout,j)
            j += 1
        }

        //Сохраняем в БД
        tasks += Task(binding.dateTaskInput.text.toString(), binding.timeInput.text.toString(),
                      binding.nameTaskInput.text.toString(),points)

        layout.setBackgroundResource(R.drawable.border_task)
        binding.layout.addView(layout)
        binding.addButton.isEnabled = true
        binding.createTaskPanel.visibility = View.GONE
        clearTaskPanel()
    }

    private fun addNewEventIntoScrollView() {
        var i = 0

        if (events.isNotEmpty())
            i = events.size

        //Сохраняем в БД
        events += Event(binding.dateInput.text.toString(), binding.timeInput.text.toString(),
                        binding.placeInput.text.toString(), binding.eventInput.text.toString())
        val textView:TextView

        if (events[i].place != ""){
            textView= mainActivity.createText("Время: " + events[i].time + System.lineSeparator() +
                                                   "Место: " + events[i].place + System.lineSeparator() +
                                                               events[i].event)
        }else if (events[i].time != ""){
            textView = mainActivity.createText("Время: " +events[i].time  + System.lineSeparator() +
                                                               events[i].event)
        }else {
            textView = mainActivity.createText(events[i].event)
        }

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                               LinearLayout.LayoutParams.WRAP_CONTENT)

        params.setMargins(15, 15, 15, 15)
        textView.setLayoutParams(params)
        textView.setBackgroundResource(R.drawable.border_event)
        textView.id = i + 10000
        binding.layout.addView(textView)

        binding.addButton.isEnabled = true
        binding.createEventPanel.visibility = View.GONE
    }

    private  fun addParamsToEditText(editText: EditText){
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                 RelativeLayout.LayoutParams.WRAP_CONTENT)
        //Установить новый пункт
        params.setMargins(30,30,30,30)
        editText.setLayoutParams(params)
        editText.setBackgroundResource(R.color.edit_text)
        editText.hint = "Пункт"
        editText.id = countOfPoint + 232320
        editText.setPadding(10,10,10,40)
        binding.pointsPlace.addView(editText)

        //Подвинуть новый пункт
        if(countOfPoint == 1) {
            params.addRule(RelativeLayout.BELOW, binding.point0.id)
        }else{
            params.addRule(RelativeLayout.BELOW, binding.createTaskPanel.findViewById<EditText>(countOfPoint + 232320-1).id)
        }
        editText.setLayoutParams(params)
    }

    @SuppressLint("ResourceType")
    private fun addParamsToNewPoint(textView: TextView, relLayout: RelativeLayout, j:Int){
        val checkBox = mainActivity.createCheckBox()
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
            binding.pointsPlace.removeView(binding.createTaskPanel.findViewById<EditText>(countOfPoint + 232320-1))
            countOfPoint -= 1
        }
    }

    private fun deletePoint() {
        if (countOfPoint > 2) {
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + 232320-1))
            countOfPoint -= 1
            addParamToButtons(binding.pointsPlace.findViewById(countOfPoint + 232320-1))
        }else if(countOfPoint > 1){
            binding.pointsPlace.removeView(binding.pointsPlace.findViewById<EditText>(countOfPoint + 232320-1))
            countOfPoint -= 1
            addParamToButtons(binding.point0)
        }else{
            mainActivity.createError("Ошибка! Нельзя удалить этот пункт!")
        }
    }
}