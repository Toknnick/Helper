package com.example.helper1

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helper1.dataBase.ApiClient
import com.example.helper1.dataBase.CreateMessageCallback
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.dataBase.GetRoomCallback
import com.example.helper1.dataBase.GetUserCallback
import com.example.helper1.dataBase.Room
import com.example.helper1.dataBase.User
import com.example.helper1.dataBase.managers.RoomManager
import com.example.helper1.dataBase.managers.UserManager
import com.example.helper1.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fm: FragmentManager
    private lateinit var dbHelper: DBHelper
    private lateinit var userManager: UserManager
    private lateinit var roomManger: RoomManager

    private var secretKey: SecretKey? = null
    private var user: User? = null
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-helper-toknnick.amvera.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(this)
        val apiClient = ApiClient(retrofit)
        roomManger = RoomManager(apiClient)
        userManager = UserManager(apiClient)
        if (secretKey == null) {
            getSecretKey()
        }
        dbHelper.updateChosenDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        binding = ActivityMainBinding.inflate(layoutInflater)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        setContentView(binding.root)



        if(dbHelper.getUser() != null) {
            val navController = findNavController(R.id.mainContainer)
            val bottomNavigationView = binding.bottomNavigationView
            bottomNavigationView.setupWithNavController(navController)
            fm = supportFragmentManager
        }
        else{
            val bottomNavigationView = this.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.visibility = View.GONE
        }

        val data: Uri? = intent?.data
        if (data != null) {
            val idRoom = data.getQueryParameter("id") // Извлекаем ID
            val password = data.getQueryParameter("password") // Извлекаем пароль

            if (idRoom != null && password != null) {
                addRoom(idRoom.toInt(), password)
            }
        }
    }

    private fun addRoom(idRoom:Int, password:String){
        user = dbHelper.getUser()
        val newRoom = Room(
            idRoom,
            "",
            password,
            false,
            "",
            "",
            ""
        )
        var availableRooms: List<Int> = ArrayList()
        if(user!!.availableRooms != "") {
            availableRooms =
                user!!.availableRooms.split("|").map { it.toInt() }.toMutableList()
        }
        if(!availableRooms.contains(newRoom.idRoom)){
            getRoomFromAPI(newRoom,false)
        }

        else{
            Toast.makeText(applicationContext,"Такая комната вам уже известна",Toast.LENGTH_LONG).show()
        }
    }

    private fun getRoomFromAPI(gettingRoom: Room, isForText: Boolean) {
        roomManger.getRoom(gettingRoom.idRoom, object : GetRoomCallback {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(gotRoom: Room) {
                gotRoom.password = unHashPassword(gotRoom.password)
                if(!gotRoom.single) {
                    if (gettingRoom.password == gotRoom.password) {
                        if (user!!.availableRooms != "") {
                            user!!.availableRooms += "|${gettingRoom.idRoom}"
                        } else {
                            user!!.availableRooms += "${gettingRoom.idRoom}"
                        }
                        if (!gotRoom.bannedUsers.contains(user!!.login)) {
                            gettingRoom.name = gotRoom.name
                            gettingRoom.owner = gotRoom.owner
                            gettingRoom.bannedUsers = gotRoom.bannedUsers
                            if (gotRoom.users != "") {
                                gettingRoom.users = gotRoom.users + "|" + user!!.login
                            } else {
                                gettingRoom.users = user!!.login
                            }
                            Toast.makeText(
                                applicationContext,
                                "Успех! Теперь вам известно на 1 комнату больше",
                                Toast.LENGTH_LONG
                            ).show()
                            updateRoomForAPI(gettingRoom)
                            updateUserForAPI(user!!)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Дверь в эту комнату вам закрыта",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка! Данные не верны!",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }else{
                    Toast.makeText(applicationContext, "Ошибка! комната не найдена!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(message: String) {
                if (!isForText) {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun updateRoomForAPI(newRoom : Room){
        newRoom.password = hashPassword(newRoom.password)
        roomManger.updateRoom(newRoom, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //nowRoom.password = unHashPassword(newRoom.password)
            }

            override fun onFailure(message: String) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserForAPI(newUser: User) {
        newUser.password = hashPassword(newUser.password)
        userManager.updateUser(newUser, object : CreateMessageCallback {
            override fun onSuccess(message: String) {
                //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                user!!.password = unHashPassword(newUser.password)
                dbHelper.updateUser(user!!)
            }

            override fun onFailure(message: String) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }, true)
    }

    protected fun saveKey(secretKey: SecretKey) {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        val prefs = applicationContext.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("aes_key", encodedKey).apply()
    }

    private fun loadKey(): SecretKey? {
        val prefs = applicationContext.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encodedKey = prefs.getString("aes_key", null) ?: return null
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    protected fun hashPassword(password: String): String {
        val cipher =
            Cipher.getInstance("AES/ECB/PKCS5Padding") // Используем PKCS5Padding для предотвращения проблем с блоками
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }


    protected fun unHashPassword(encryptedPassword: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding") // Совпадение с шифрованием
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun checkIsHaveLog(){
        val bottomNavigationView =
            this.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }

    fun startActivity(){
        val navController = findNavController(R.id.mainContainer)
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
        fm = supportFragmentManager
        bottomNavigationView.selectedItemId = R.id.homeFragment
        bottomNavigationView.visibility = View.VISIBLE
    }

    private fun getSecretKey() {
        userManager.getUser("FirstUser", object : GetUserCallback {
            override fun onSuccess(user: User) {
                val key = user.password
                secretKey = stringToKey(key)
                saveKey(secretKey!!)
            }

            override fun onFailure(isExist: Boolean) {}
        })
    }

    private fun stringToKey(keyString: String): SecretKey {
        val decodedKey = Base64.decode(keyString, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
    override fun onStop() {
        super.onStop()
        val dbHelper = DBHelper(applicationContext)
        dbHelper.updateChosenDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
    }
}