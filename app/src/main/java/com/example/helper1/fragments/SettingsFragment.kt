package com.example.helper1.fragments

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helper1.MainActivity
import com.example.helper1.R
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.dataBase.GetUserCallback
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.UserManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private lateinit var userManager: UserManager
    private lateinit var user: User
    private lateinit var dbHelper: DBHelper
    private lateinit var mainActivity: MainActivity
    private lateinit var editeloginUserButton: ImageButton
    private lateinit var editeUserPanel: RelativeLayout
    protected var secretKey: SecretKey? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        return view
    }

    override fun onResume() {
        super.onResume()
        secretKey = loadKey()

        if (secretKey==null){
            secretKey = generateKey()
            saveKey(secretKey!!)
        }

        val apiClient = ApiClient(retrofit)
        userManager = UserManager(apiClient)
        val editUserButton = requireView().findViewById<ImageButton>(R.id.editUserButton)
        val userLoginTextView = requireView().findViewById<TextView>(R.id.userLoginTextView)
        editeUserPanel = requireView().findViewById<RelativeLayout>(R.id.editeUserPanel)
        val editeloginUser = requireView().findViewById<EditText>(R.id.editeloginUser)
        val editepasswordUser = requireView().findViewById<EditText>(R.id.editepasswordUser)
        editeloginUserButton = requireView().findViewById<ImageButton>(R.id.editeloginUserButton)
        val editebackUserButton = requireView().findViewById<ImageButton>(R.id.editebackUserButton)

        val logOutButton = requireView().findViewById<Button>(R.id.logOutButton)

        val createUserPanel = requireView().findViewById<RelativeLayout>(R.id.createUserPanel)
        val passwordUser = requireView().findViewById<EditText>(R.id.passwordUser)
        val loginUser = requireView().findViewById<EditText>(R.id.loginUser)
        val loginUserButton = requireView().findViewById<Button>(R.id.loginUserButton)

        dbHelper = DBHelper(requireContext())
        user = dbHelper.getUser()!!
        userLoginTextView.text = user.login
        mainActivity = (activity as MainActivity)

        editUserButton.setOnClickListener{
            editeUserPanel.visibility = View.VISIBLE
            editeloginUser.setText(user.login)
            editepasswordUser.setText(user.password)
            editeloginUser.isEnabled = false
        }
        editebackUserButton.setOnClickListener{
            editeUserPanel.visibility = View.GONE
            editeloginUser.setText("")
            editepasswordUser.setText("")
        }
        editeloginUserButton.setOnClickListener{
            if(editepasswordUser.text.toString().trim().length > 8){
                user.login = editeloginUser.text.toString().trim()
                user.password = editepasswordUser.text.toString().trim()
                dbHelper.updateUser(user)
                updateUserForAPI(user)
                editeUserPanel.visibility = View.GONE
                editeloginUser.setText("")
                editepasswordUser.setText("")
            }
            else{
                Toast.makeText(requireContext(), "Малая длина пароля", Toast.LENGTH_SHORT).show()
            }

        }
        logOutButton.setOnClickListener{
            requireContext().deleteDatabase(dbHelper.databaseName)
            user = User("","",-1,"")
            userLoginTextView.setText("Вы не вошли в аккаунт")
            createUserPanel.visibility = View.VISIBLE
            mainActivity.checkIsHaveLog()
            editeloginUserButton.isEnabled = false
            editeUserPanel.visibility = View.GONE
            editeloginUser.setText("")
            editepasswordUser.setText("")
        }
        loginUserButton.setOnClickListener{
            val user = User(loginUser.text.toString().trim(),passwordUser.text.toString().trim(),0,"")
            loginUserForAPI(user)
        }

        Toast.makeText(requireContext(), user!!.password, Toast.LENGTH_LONG).show()

    }

    private fun loginUserForAPI(loggingUser: User) {
        userManager.getUser(loggingUser.login, object : GetUserCallback {
            override fun onSuccess(gotUser: User) {
                if (loggingUser.password == unHashPassword(gotUser.password)) {
                    gotUser.password = loggingUser.password
                    user = gotUser
                    var db = DBHelper(requireContext())
                    db.createUser(user)
                    val userLoginTextView = requireView().findViewById<TextView>(R.id.userLoginTextView)
                    userLoginTextView.text = user.login
                    val createUserPanel = requireView().findViewById<View>(R.id.createUserPanel)
                    createUserPanel.visibility = View.GONE
                    mainActivity.startActivity()
                    editeloginUserButton.isEnabled = true

                } else {
                    Toast.makeText(requireContext(), "Неверный пароль!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(isExist: Boolean) {
                Toast.makeText(requireContext(), "Ошибка! Пользователь не найден", Toast.LENGTH_LONG).show()
            }
        })

        Log.d("MyTag", loggingUser.toString())
    }


    private fun updateUserForAPI(newUser: User) {
        newUser.password = hashPassword(newUser.password)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-helper-toknnick.amvera.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val userManager = UserManager(ApiClient(retrofit))
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
            }

            override fun onFailure(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, true)
    }

    private fun saveKey(secretKey: SecretKey) {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        val prefs = requireContext().getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("aes_key", encodedKey).apply()
    }

    private fun loadKey(): SecretKey? {
        val prefs = requireContext().getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encodedKey = prefs.getString("aes_key", null) ?: return null
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    private fun hashPassword(password: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding") // Используем PKCS5Padding для предотвращения проблем с блоками
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }


    private fun unHashPassword(encryptedPassword: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding") // Совпадение с шифрованием
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

}