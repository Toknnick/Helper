package com.example.helper1

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helper1.dataBase.DBHelper
import com.example.helper1.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DBHelper(this)
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
    }

    fun startActivity(){
        val navController = findNavController(R.id.mainContainer)
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
        fm = supportFragmentManager
        bottomNavigationView.selectedItemId = R.id.homeFragment
    }

    override fun onStop() {
        super.onStop()
        val dbHelper = DBHelper(applicationContext)
        dbHelper.updateChosenDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
    }
}