package com.example.helper1

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.helper1.databinding.FragmentHomeBinding


class HomeFragment :  Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
    }

    @SuppressLint("ResourceAsColor")
    fun getText(newText: TextView){
        binding.layout.addView(newText)
        Log.v("MyTag","color"+newText.background)
    }
}