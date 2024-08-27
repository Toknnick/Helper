package com.example.helper1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.helper1.databinding.FragmentRoomsBinding

class RoomsFragment : Fragment() {
    private lateinit var binding: FragmentRoomsBinding
    //TODO: менять у нынешнего пользователя availableRooms после подключения к комнате

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveUserButton.setOnClickListener {
            createUser()
        }
        binding.updateUserButton.setOnClickListener {
            updateUser()
        }
    }

}