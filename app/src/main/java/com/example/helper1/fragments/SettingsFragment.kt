package com.example.helper1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helper1.R
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.UserManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        return view
    }

    override fun onResume() {
        super.onResume()
        val editUserButton = requireView().findViewById<ImageButton>(R.id.editUserButton)
        val userLoginTextView = requireView().findViewById<TextView>(R.id.userLoginTextView)
        val createUserPanel = requireView().findViewById<RelativeLayout>(R.id.createUserPanel)
        val loginUser = requireView().findViewById<EditText>(R.id.loginUser)
        val passwordUser = requireView().findViewById<EditText>(R.id.passwordUser)
        val loginUserButton = requireView().findViewById<ImageButton>(R.id.loginUserButton)
        val backUserButton = requireView().findViewById<ImageButton>(R.id.backUserButton)

        val dbHelper = DBHelper(requireContext())
        val user = dbHelper.getUser()
        userLoginTextView.text = user!!.login

        editUserButton.setOnClickListener{
            createUserPanel.visibility = View.VISIBLE
            loginUser.setText(user.login)
            passwordUser.setText(user.password)
            loginUser.isEnabled = false
        }
        backUserButton.setOnClickListener{
            createUserPanel.visibility = View.GONE
            loginUser.setText("")
            passwordUser.setText("")
        }
        loginUserButton.setOnClickListener{
            if(passwordUser.text.toString().trim().length > 8){
                user.login = loginUser.text.toString().trim()
                user.password = passwordUser.text.toString().trim()
                dbHelper.updateUser(user)
                updateUserForAPI(user)
                createUserPanel.visibility = View.GONE
                loginUser.setText("")
                passwordUser.setText("")
            }
            else{
                Toast.makeText(requireContext(), "Малая длина пароля", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun updateUserForAPI(newUser: User) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-helper-toknnick.amvera.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val userManager = UserManager(ApiClient(retrofit))
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {}

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, true)
    }
}