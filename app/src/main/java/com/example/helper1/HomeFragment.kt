package com.example.helper1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.helper1.databinding.FragmentHomeBinding

data class Event(
    var data: String,
    var time: String,
    var place: String,
    var event: String
)

class HomeFragment :  Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var events: List<Event>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        events = ArrayList()
        binding = FragmentHomeBinding.inflate(inflater)
        //createSpinner()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = (activity as MainActivity)

        binding.addButton.setOnClickListener{
            mainActivity.showDialog(this)
        }
        binding.saveButton.setOnClickListener{
            addNewEventIntoScrollView()
        }
    }

    fun createNewText(item: Int){
        if (item == 0) {
            //Делаем задачу
        } else {
            //Делаем событие
            binding.createEventPanel.visibility = View.VISIBLE
            //
        }
    }

    private fun addNewEventIntoScrollView(){
        var i = 0

        if(events.isNotEmpty())
            i = events.size

        events += Event(binding.dateInput.text.toString(),binding.timeInput.text.toString(),
            binding.placeInput.text.toString(),binding.eventInput.text.toString())

        binding.createEventPanel.visibility = View.GONE

        binding.dateInput.setText("")
        binding.timeInput.setText("")
        binding.placeInput.setText("")
        binding.eventInput.setText("")

        binding.layout.addView(mainActivity.createText("Время: " +events[i].time + System.lineSeparator() +
                                                            "Место: "  +events[i].place + System.lineSeparator() +
                                                                      events[i].event, R.color.green))
    }
}