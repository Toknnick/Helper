package com.example.helper1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.MySQLController
import com.example.helper1.dataBase.User
import com.example.helper1.databinding.FragmentRoomsBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomsFragment : Fragment() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()



    private lateinit var binding: FragmentRoomsBinding
    private lateinit var mysqlController : MySQLController
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
        val apiClient = ApiClient(retrofit)
        mysqlController = MySQLController(apiClient)
        binding.saveUserButton.setOnClickListener {
            createUserForAPI()
        }
        binding.updateUserButton.setOnClickListener {
            updateUserForAPI()
        }
    }

    private fun createUserForAPI(){
        val newUser = User(binding.loginUser.text.toString().trim(),binding.passwordUser.text.toString().trim(),"")
        mysqlController.createUser(newUser, object : MySQLController.CreateUserCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserForAPI(){
        val newUser = User(binding.loginUser.text.toString().trim(),binding.passwordUser.text.toString().trim(),"")
        mysqlController.updateUser(newUser, object : MySQLController.CreateUserCallback {
            override fun onSuccess(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

}